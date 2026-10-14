package com.studymate.app.data.util

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.ByteArrayInputStream
import java.io.IOException
import java.util.zip.ZipInputStream

/**
 * Exceptions describing specific extraction failure modes.
 */
class ScannedPdfException(message: String) : Exception(message)
class EmptyDocumentException(message: String) : Exception(message)
class PdfExtractionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * Safely extracts textual content from study materials (PDF, DOCX, PPTX, and TXT).
 *
 * Enforces safety boundaries:
 * - Scanned PDFs (image-only with < 40 extractable characters) are detected and rejected with [ScannedPdfException].
 * - Completely empty files are caught with [EmptyDocumentException].
 * - DOCX & PPTX files are natively unzipped and parsed from their XML content without heavy external libraries.
 * - Large documents are bounded to the first 25 pages / 40,000 characters (~10,000 tokens) with a notice.
 * - Resource leaks are prevented via deterministic closing.
 * - Unsupported or image formats provide clear, helpful student guidance.
 */
object PdfTextExtractor {

    private const val MAX_PAGES = 25
    private const val MAX_CHARACTERS = 40_000
    private const val MIN_CHARACTERS_FOR_SCANNED_CHECK = 40

    @Volatile
    private var isInitialized = false

    private fun ensureInitialized(context: Context) {
        if (!isInitialized) {
            synchronized(this) {
                if (!isInitialized) {
                    PDFBoxResourceLoader.init(context.applicationContext)
                    isInitialized = true
                }
            }
        }
    }

    /**
     * Extracts text from the provided [fileBytes] based on [fileType].
     */
    fun extractText(
        context: Context,
        fileBytes: ByteArray,
        fileType: String = "PDF"
    ): Result<String> {
        if (fileBytes.isEmpty()) {
            return Result.failure(EmptyDocumentException("The selected file is empty (0 bytes)."))
        }

        val type = fileType.uppercase().trim()

        return when {
            type == "TXT" -> {
                Result.failure(
                    PdfExtractionException("Summaries are not supported for plain text (.txt) files. Please upload your course notes as a PDF, DOCX, or PPTX document.")
                )
            }
            type in listOf("DOCX", "DOC") -> {
                if (isZipArchive(fileBytes)) {
                    extractDocxText(fileBytes)
                } else {
                    Result.failure(
                        PdfExtractionException("Older legacy .doc files are not supported. Please save as a modern Word document (.docx) or PDF.")
                    )
                }
            }
            type in listOf("PPTX", "PPT") -> {
                if (isZipArchive(fileBytes)) {
                    extractPptxText(fileBytes)
                } else {
                    Result.failure(
                        PdfExtractionException("Older legacy .ppt files are not supported. Please save as a modern PowerPoint (.pptx) or PDF.")
                    )
                }
            }
            type in listOf("JPG", "JPEG", "PNG", "WEBP", "IMAGE") -> {
                Result.failure(
                    PdfExtractionException("Summaries are not supported for image files. Please upload your course notes as a PDF, DOCX, or PPTX document.")
                )
            }
            type == "YOUTUBE" -> {
                Result.failure(
                    PdfExtractionException("Summaries are not supported for YouTube videos. Please upload your course notes as a PDF, DOCX, or PPTX document.")
                )
            }
            else -> {
                // Default to PDF parser
                extractPdf(context, fileBytes)
            }
        }
    }

    private fun isZipArchive(bytes: ByteArray): Boolean {
        return bytes.size >= 4 && bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte()
    }

    private fun extractTxt(fileBytes: ByteArray): Result<String> = runCatching {
        val text = String(fileBytes, Charsets.UTF_8).trim()
        if (text.isBlank()) {
            throw EmptyDocumentException("The text file contains no readable content.")
        }
        boundText(text)
    }

    private fun extractDocxText(fileBytes: ByteArray): Result<String> = runCatching {
        val stringBuilder = StringBuilder()
        ZipInputStream(ByteArrayInputStream(fileBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == "word/document.xml") {
                    val xmlContent = zis.bufferedReader(Charsets.UTF_8).readText()
                    // Extract all text inside <w:t> tags
                    val regex = Regex("<w:t[^>]*>(.*?)</w:t>")
                    val matches = regex.findAll(xmlContent)
                    for (match in matches) {
                        stringBuilder.append(match.groupValues[1]).append(" ")
                    }
                    break
                }
                entry = zis.nextEntry
            }
        }
        val text = stringBuilder.toString().trim()
        if (text.isBlank()) {
            throw EmptyDocumentException("The Word document (.docx) contains no readable text.")
        }
        boundText(text)
    }

    private fun extractPptxText(fileBytes: ByteArray): Result<String> = runCatching {
        val stringBuilder = StringBuilder()
        ZipInputStream(ByteArrayInputStream(fileBytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name.startsWith("ppt/slides/slide") && entry.name.endsWith(".xml")) {
                    val xmlContent = zis.bufferedReader(Charsets.UTF_8).readText()
                    // Extract all text inside <a:t> tags in slides
                    val regex = Regex("<a:t[^>]*>(.*?)</a:t>")
                    val matches = regex.findAll(xmlContent)
                    for (match in matches) {
                        stringBuilder.append(match.groupValues[1]).append(" ")
                    }
                    stringBuilder.append("\n\n")
                }
                entry = zis.nextEntry
            }
        }
        val text = stringBuilder.toString().trim()
        if (text.isBlank()) {
            throw EmptyDocumentException("The PowerPoint presentation (.pptx) contains no readable text in its slides.")
        }
        boundText(text)
    }

    private fun extractPdf(context: Context, fileBytes: ByteArray): Result<String> {
        return try {
            ensureInitialized(context)

            ByteArrayInputStream(fileBytes).use { inputStream ->
                PDDocument.load(inputStream).use { document ->
                    if (document.isEncrypted) {
                        return Result.failure(
                            PdfExtractionException("The PDF document is password-protected or encrypted. Please provide an unencrypted PDF.")
                        )
                    }

                    val pageCount = document.numberOfPages
                    if (pageCount == 0) {
                        return Result.failure(
                            EmptyDocumentException("The PDF document contains 0 pages.")
                        )
                    }

                    val stripper = PDFTextStripper().apply {
                        startPage = 1
                        endPage = minOf(pageCount, MAX_PAGES)
                    }

                    val extracted = stripper.getText(document).trim()

                    // Check for scanned / image-only PDFs
                    if (extracted.length < MIN_CHARACTERS_FOR_SCANNED_CHECK) {
                        return Result.failure(
                            ScannedPdfException(
                                "This PDF appears to be a scanned document or image-only file with no selectable text. " +
                                "Gemini requires readable text to generate study notes. Please use a text-based PDF or apply OCR first."
                            )
                        )
                    }

                    // Handle large documents gracefully
                    val truncatedText = when {
                        extracted.length > MAX_CHARACTERS -> {
                            extracted.take(MAX_CHARACTERS) +
                                "\n\n[Note: Extracted text was truncated at $MAX_CHARACTERS characters to comply with input limits.]"
                        }
                        pageCount > MAX_PAGES -> {
                            extracted +
                                "\n\n[Note: Analyzed first $MAX_PAGES of $pageCount pages to ensure high-quality synthesis.]"
                        }
                        else -> extracted
                    }

                    Result.success(truncatedText)
                }
            }
        } catch (e: ScannedPdfException) {
            Result.failure(e)
        } catch (e: EmptyDocumentException) {
            Result.failure(e)
        } catch (e: OutOfMemoryError) {
            Result.failure(
                PdfExtractionException("The document is too large to process in device memory. Please choose a smaller document.")
            )
        } catch (e: IOException) {
            Result.failure(
                PdfExtractionException("Unable to read or parse PDF structure: ${e.localizedMessage ?: "Corrupt file"}", e)
            )
        } catch (e: Exception) {
            Result.failure(
                PdfExtractionException("Text extraction failed: ${e.localizedMessage ?: "Unknown parsing error"}", e)
            )
        }
    }

    private fun boundText(text: String): String {
        return if (text.length > MAX_CHARACTERS) {
            text.take(MAX_CHARACTERS) + "\n\n[Note: Document truncated at $MAX_CHARACTERS characters due to model input limits.]"
        } else {
            text
        }
    }
}
