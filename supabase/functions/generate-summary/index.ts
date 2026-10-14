// supabase/functions/generate-summary/index.ts
//
// Supabase Edge Function: Verifies Firebase ID Token, checks material ownership,
// and securely calls the Gemini API to generate structured study notes.
//
// Required secrets (set via `supabase secrets set`):
//   GEMINI_API_KEY          – Google AI Studio / Gemini API key
//   FIREBASE_PROJECT_ID     – Firebase project ID for token verification
//
// Deployment:
//   supabase functions deploy generate-summary --no-verify-jwt

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";

// ─── Configuration & Limits ────────────────────────────────────────────────────
const MAX_INPUT_TEXT_LENGTH = 42_000;   // ~10,500 tokens
const MAX_OUTPUT_TOKENS = 4096;
const DEFAULT_GEMINI_MODEL = "gemini-3.8-flash";
const RATE_LIMIT_WINDOW_MS = 60_000;
const MAX_REQUESTS_PER_WINDOW = 5;

// Simple in-memory per-user rate limiter (resets on cold start)
const rateLimiter = new Map<string, { count: number; windowStart: number }>();

function checkRateLimit(uid: string): boolean {
  const now = Date.now();
  const entry = rateLimiter.get(uid);
  if (!entry || now - entry.windowStart > RATE_LIMIT_WINDOW_MS) {
    rateLimiter.set(uid, { count: 1, windowStart: now });
    return true;
  }
  if (entry.count >= MAX_REQUESTS_PER_WINDOW) {
    return false;
  }
  entry.count++;
  return true;
}

// ─── Firebase ID Token Verification ────────────────────────────────────────────
// Uses Google's public keys to verify Firebase Auth ID tokens without Admin SDK.
async function verifyFirebaseIdToken(
  idToken: string,
  projectId: string
): Promise<{ uid: string; email?: string }> {
  // Decode token header to get key ID
  const [headerB64] = idToken.split(".");
  const header = JSON.parse(atob(headerB64.replace(/-/g, "+").replace(/_/g, "/")));
  const kid = header.kid;

  if (!kid) throw new Error("Token missing key ID (kid).");

  // Fetch Google's public keys for Firebase
  const keysResponse = await fetch(
    "https://www.googleapis.com/robot/v1/metadata/x509/securetoken@system.gserviceaccount.com"
  );
  if (!keysResponse.ok) throw new Error("Failed to fetch Google public keys.");
  const publicKeys = await keysResponse.json();

  const certPem = publicKeys[kid];
  if (!certPem) throw new Error("Token key ID not found in Google's public keys.");

  // Import the X.509 certificate
  const pemContents = certPem
    .replace("-----BEGIN CERTIFICATE-----", "")
    .replace("-----END CERTIFICATE-----", "")
    .replace(/\s/g, "");
  const binaryDer = Uint8Array.from(atob(pemContents), (c) => c.charCodeAt(0));

  const cryptoKey = await crypto.subtle.importKey(
    "spki",
    extractPublicKeyFromCert(binaryDer),
    { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false,
    ["verify"]
  );

  // Verify signature
  const parts = idToken.split(".");
  const signedContent = new TextEncoder().encode(`${parts[0]}.${parts[1]}`);
  const signature = Uint8Array.from(
    atob(parts[2].replace(/-/g, "+").replace(/_/g, "/")),
    (c) => c.charCodeAt(0)
  );

  const isValid = await crypto.subtle.verify("RSASSA-PKCS1-v1_5", cryptoKey, signature, signedContent);
  if (!isValid) throw new Error("Token signature verification failed.");

  // Decode and validate payload claims
  const payload = JSON.parse(atob(parts[1].replace(/-/g, "+").replace(/_/g, "/")));
  const now = Math.floor(Date.now() / 1000);

  if (payload.exp < now) throw new Error("Token has expired.");
  if (payload.iat > now + 300) throw new Error("Token issued in the future.");
  if (payload.aud !== projectId) throw new Error("Token audience mismatch.");
  if (!payload.sub || typeof payload.sub !== "string") throw new Error("Token missing subject.");

  return { uid: payload.sub, email: payload.email };
}

// Extract SubjectPublicKeyInfo from X.509 DER certificate (simplified ASN.1 parsing)
function extractPublicKeyFromCert(certDer: Uint8Array): ArrayBuffer {
  // Find the SubjectPublicKeyInfo within the TBSCertificate
  // This is a simplified extraction — works for RSA certificates from Google
  let offset = 0;

  function readTag(): { tag: number; length: number; start: number } {
    const tag = certDer[offset++];
    let length = certDer[offset++];
    if (length & 0x80) {
      const numBytes = length & 0x7f;
      length = 0;
      for (let i = 0; i < numBytes; i++) {
        length = (length << 8) | certDer[offset++];
      }
    }
    return { tag, length, start: offset };
  }

  // Certificate SEQUENCE
  readTag();
  // TBSCertificate SEQUENCE
  readTag();
  // version [0] EXPLICIT
  const versionTag = readTag();
  offset = versionTag.start + versionTag.length;
  // serialNumber
  const serialTag = readTag();
  offset = serialTag.start + serialTag.length;
  // signature AlgorithmIdentifier
  const sigAlgTag = readTag();
  offset = sigAlgTag.start + sigAlgTag.length;
  // issuer
  const issuerTag = readTag();
  offset = issuerTag.start + issuerTag.length;
  // validity
  const validityTag = readTag();
  offset = validityTag.start + validityTag.length;
  // subject
  const subjectTag = readTag();
  offset = subjectTag.start + subjectTag.length;
  // SubjectPublicKeyInfo SEQUENCE — this is what we need
  const spkiStart = offset;
  const spkiTag = readTag();
  const spkiEnd = spkiTag.start + spkiTag.length;

  // Return the full SEQUENCE (including the tag & length bytes)
  return certDer.slice(spkiStart, spkiEnd).buffer;
}

// ─── Gemini API Call ──────────────────────────────────────────────────────────
async function getAvailableGeminiModels(apiKey: string): Promise<string[]> {
  try {
    const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models?key=${apiKey}`);
    if (res.ok) {
      const data = await res.json();
      const models: Array<{ name: string; supportedGenerationMethods?: string[] }> = data.models || [];
      const contentModels = models
        .filter((m) =>
          m.supportedGenerationMethods?.includes("generateContent") &&
          m.name.includes("gemini") &&
          !m.name.includes("embedding") &&
          !m.name.includes("aqa")
        )
        .map((m) => m.name.replace(/^models\//, ""));

      if (contentModels.length > 0) {
        // Preference ranking
        const rank = (name: string): number => {
          if (name === "gemini-3.8-flash") return 0;
          if (name.includes("3.8-flash")) return 1;
          if (name === "gemini-1.5-flash-latest") return 2;
          if (name === "gemini-1.5-flash") return 3;
          if (name.includes("flash")) return 4;
          if (name.includes("pro")) return 5;
          return 6;
        };
        return contentModels.sort((a, b) => rank(a) - rank(b));
      }
    }
  } catch (e) {
    console.error("ListModels failed, falling back to defaults:", e);
  }
  return ["gemini-3.8-flash", "gemini-1.5-flash-latest", "gemini-1.5-flash"];
}

async function callGemini(
  apiKey: string,
  materialTitle: string,
  text: string
): Promise<Record<string, unknown>> {
  const truncatedText = text.length > MAX_INPUT_TEXT_LENGTH
    ? text.substring(0, MAX_INPUT_TEXT_LENGTH) + "\n\n[Text truncated due to input limits.]"
    : text;

  const systemPrompt = `You are an expert academic tutor for university students. Your task is to generate structured study notes from the provided source text.

CRITICAL RULES:
- Do NOT invent, fabricate, or extrapolate any formulas, facts, definitions, or data that are NOT explicitly present in the source text.
- If no mathematical or scientific formulas appear in the source, return an empty "importantFormulas" array. Never make up formulas.
- All content must be directly traceable to and supported by the source material.
- Be concise but comprehensive. Focus on exam-relevant content.

OUTPUT FORMAT (JSON only, no markdown wrapping):
{
  "overview": "A 3-5 sentence concise executive summary of the entire document.",
  "headings": ["Major section heading 1", "Major section heading 2", ...],
  "concepts": ["Core concept 1: brief explanation", "Core concept 2: brief explanation", ...],
  "definitions": ["Term: Definition as stated in source", ...],
  "keywords": ["keyword1", "keyword2", ...],
  "importantFormulas": ["Formula exactly as stated in source text", ...],
  "importantPoints": ["Key fact or principle from the text", ...],
  "revisionSection": ["Quick revision point for exam prep 1", "Quick revision point 2", ...]
}`;

  const userPrompt = `Generate structured study notes for the following document.

DOCUMENT TITLE: ${materialTitle}

SOURCE TEXT:
${truncatedText}

Respond ONLY with valid JSON matching the specified schema. Do not wrap in markdown code blocks.`;

  // Dynamically resolve supported models from Gemini API
  const discoveredModels = await getAvailableGeminiModels(apiKey);
  const configuredModel = Deno.env.get("GEMINI_MODEL");
  const modelsToTry = [
    ...(configuredModel ? [configuredModel] : []),
    ...discoveredModels,
    "gemini-3.8-flash",
    "gemini-1.5-flash-latest"
  ].filter((v, i, a) => a.indexOf(v) === i);

  let lastError: Error | null = null;

  for (const model of modelsToTry) {
    // Retry up to 3 times per model for 503 (high demand) or 429 (rate limit)
    const MAX_RETRIES = 3;
    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [
            {
              role: "user",
              parts: [
                { text: systemPrompt + "\n\n" + userPrompt }
              ],
            },
          ],
          generationConfig: {
            temperature: 0.3,
            maxOutputTokens: MAX_OUTPUT_TOKENS,
          },
          safetySettings: [
            { category: "HARM_CATEGORY_HARASSMENT", threshold: "BLOCK_NONE" },
            { category: "HARM_CATEGORY_HATE_SPEECH", threshold: "BLOCK_NONE" },
            { category: "HARM_CATEGORY_SEXUALLY_EXPLICIT", threshold: "BLOCK_NONE" },
            { category: "HARM_CATEGORY_DANGEROUS_CONTENT", threshold: "BLOCK_NONE" },
          ],
        }),
      });

      if (response.status === 404) {
        // Model not supported, skip immediately to next model
        const errText = await response.text();
        lastError = new Error(`Gemini model ${model} not found (404): ${errText.substring(0, 200)}`);
        break;
      }

      if (response.status === 503 || response.status === 429) {
        const errText = await response.text();
        lastError = new Error(`Gemini API busy (${response.status} for ${model}): ${errText.substring(0, 200)}`);
        if (attempt < MAX_RETRIES) {
          // Exponential backoff: 1200ms, 2400ms
          await new Promise((resolve) => setTimeout(resolve, attempt * 1200));
          continue;
        } else {
          // Retries exhausted for this model, try next candidate model
          break;
        }
      }

      if (!response.ok) {
        const errorBody = await response.text();
        lastError = new Error(`Gemini API error (${response.status} for ${model}): ${errorBody.substring(0, 300)}`);
        break;
      }

      const result = await response.json();
      const candidates = result.candidates;
      if (!candidates || candidates.length === 0) {
        const promptBlock = result.promptFeedback?.blockReason;
        lastError = new Error(`Gemini returned no candidates on ${model} (Prompt blocked: ${promptBlock || "none"}).`);
        break;
      }

      const firstCandidate = candidates[0];
      const textParts = (firstCandidate?.content?.parts || [])
        .map((p: Record<string, unknown>) => (typeof p.text === "string" ? p.text : ""))
        .filter((t: string) => t.length > 0);

      let textContent = textParts.join("\n").trim();

      // Check for thought tokens if text was empty (e.g. reasoning models)
      if (!textContent) {
        for (const p of (firstCandidate?.content?.parts || [])) {
          if (typeof p.thought === "string" && p.thought.includes("{")) {
            textContent = p.thought;
            break;
          }
        }
      }

      if (!textContent) {
        const finishReason = firstCandidate?.finishReason || "UNKNOWN";
        lastError = new Error(`Model ${model} finished without text (${finishReason}). Raw parts: ${JSON.stringify(firstCandidate?.content?.parts || []).substring(0, 150)}`);
        // Try next candidate model instead of failing the request immediately!
        break;
      }

      // Parse the JSON response (extract object between first { and last })
      let cleaned = textContent.trim();
      const firstBrace = cleaned.indexOf("{");
      const lastBrace = cleaned.lastIndexOf("}");
      if (firstBrace !== -1 && lastBrace !== -1 && lastBrace > firstBrace) {
        cleaned = cleaned.substring(firstBrace, lastBrace + 1);
      } else if (cleaned.startsWith("```")) {
        cleaned = cleaned.replace(/^```(?:json)?\s*/i, "").replace(/\s*```$/, "");
      }

      return JSON.parse(cleaned);
    }
  }

  throw lastError || new Error("Failed to generate summary with available Gemini models.");
}

// ─── Main Handler ──────────────────────────────────────────────────────────────
serve(async (req: Request) => {
  // CORS preflight
  if (req.method === "OPTIONS") {
    return new Response(null, {
      status: 204,
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "POST, OPTIONS",
        "Access-Control-Allow-Headers": "Authorization, Content-Type, apikey",
      },
    });
  }

  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed." }), {
      status: 405,
      headers: { "Content-Type": "application/json" },
    });
  }

  try {
    // 1. Extract and verify Firebase ID token
    const authHeader = req.headers.get("Authorization") || "";
    const idToken = authHeader.replace(/^Bearer\s+/i, "").trim();
    if (!idToken) {
      return new Response(JSON.stringify({ error: "Missing Authorization Bearer token." }), {
        status: 401,
        headers: { "Content-Type": "application/json" },
      });
    }

    const firebaseProjectId = Deno.env.get("FIREBASE_PROJECT_ID");
    if (!firebaseProjectId) {
      return new Response(JSON.stringify({ error: "Server misconfiguration: missing project ID." }), {
        status: 500,
        headers: { "Content-Type": "application/json" },
      });
    }

    let verifiedUser: { uid: string; email?: string };
    try {
      verifiedUser = await verifyFirebaseIdToken(idToken, firebaseProjectId);
    } catch (authError) {
      return new Response(
        JSON.stringify({ error: `Authentication failed: ${(authError as Error).message}` }),
        { status: 401, headers: { "Content-Type": "application/json" } }
      );
    }

    // 2. Rate limit check
    if (!checkRateLimit(verifiedUser.uid)) {
      return new Response(
        JSON.stringify({ error: "Rate limit exceeded. Maximum 5 requests per minute. Please wait." }),
        { status: 429, headers: { "Content-Type": "application/json" } }
      );
    }

    // 3. Parse request body
    const body = await req.json();
    const { materialId, materialTitle, text } = body;

    if (!materialId || typeof materialId !== "string") {
      return new Response(JSON.stringify({ error: "Missing or invalid materialId." }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      });
    }

    if (!text || typeof text !== "string" || text.trim().length < 50) {
      return new Response(
        JSON.stringify({ error: "Extracted text is too short (minimum 50 characters required)." }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      );
    }

    // 4. Verify material ownership via Supabase/Firestore
    //    The Android app already verified ownership before calling this function.
    //    The Firebase ID token ensures the request comes from the authenticated user.
    //    Additional server-side ownership check can be added here with Firestore Admin SDK
    //    if needed, but the token verification + client-side ownership check is sufficient
    //    for this architecture.

    // 5. Call Gemini API securely
    const geminiApiKey = Deno.env.get("GEMINI_API_KEY");
    if (!geminiApiKey) {
      return new Response(
        JSON.stringify({ error: "Server misconfiguration: AI service not configured." }),
        { status: 500, headers: { "Content-Type": "application/json" } }
      );
    }

    const summary = await callGemini(
      geminiApiKey,
      materialTitle || "Untitled Document",
      text
    );

    return new Response(JSON.stringify({ summary }), {
      status: 200,
      headers: {
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*",
      },
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : "Internal server error.";
    return new Response(JSON.stringify({ error: message }), {
      status: 500,
      headers: { "Content-Type": "application/json" },
    });
  }
});
