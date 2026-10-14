// supabase/functions/generate-quiz/index.ts
// Deployment: supabase functions deploy generate-quiz --no-verify-jwt

import { serve } from "https://deno.land/std@0.168.0/http/server.ts";

const MAX_INPUT_TEXT_LENGTH = 45_000;

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
        const rank = (name: string): number => {
          if (name === "gemini-2.0-flash") return 0;
          if (name === "gemini-1.5-flash") return 1;
          if (name === "gemini-1.5-flash-latest") return 2;
          if (name === "gemini-2.0-flash-lite") return 3;
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
  return [
    "gemini-2.0-flash",
    "gemini-1.5-flash",
    "gemini-1.5-flash-latest",
    "gemini-2.0-flash-lite",
  ];
}

function extractCandidateText(candidate: Record<string, unknown>): string {
  const content = candidate?.content as Record<string, unknown> | undefined;
  if (!content?.parts || !Array.isArray(content.parts)) return "";
  const parts = content.parts as Array<Record<string, unknown>>;

  // 1. Check all text fields
  const textParts = parts
    .map((p) => (typeof p.text === "string" ? p.text : ""))
    .filter((t) => t.trim().length > 0);

  if (textParts.length > 0) {
    return textParts.join("\n").trim();
  }

  // 2. Check thought fields (reasoning models)
  for (const p of parts) {
    if (typeof p.thought === "string" && (p.thought.includes("{") || p.thought.includes("["))) {
      return p.thought;
    }
  }

  return "";
}

function parseQuizJson(raw: string): { questions: unknown[] } {
  let cleaned = raw.trim();
  cleaned = cleaned.replace(/^```json\s*/i, "").replace(/^```\s*/i, "").replace(/\s*```$/i, "").trim();

  // 1. Direct parse
  try {
    const obj = JSON.parse(cleaned);
    if (obj && Array.isArray(obj.questions) && obj.questions.length > 0) return obj;
    if (Array.isArray(obj) && obj.length > 0) return { questions: obj };
  } catch (_) {}

  // 2. Outermost braces { ... }
  const firstBrace = cleaned.indexOf("{");
  const lastBrace = cleaned.lastIndexOf("}");
  if (firstBrace !== -1 && lastBrace > firstBrace) {
    try {
      const obj = JSON.parse(cleaned.substring(firstBrace, lastBrace + 1));
      if (obj && Array.isArray(obj.questions) && obj.questions.length > 0) return obj;
      if (Array.isArray(obj) && obj.length > 0) return { questions: obj };
    } catch (_) {}
  }

  // 3. Outermost brackets [ ... ]
  const firstBracket = cleaned.indexOf("[");
  const lastBracket = cleaned.lastIndexOf("]");
  if (firstBracket !== -1 && lastBracket > firstBracket) {
    try {
      const arr = JSON.parse(cleaned.substring(firstBracket, lastBracket + 1));
      if (Array.isArray(arr) && arr.length > 0) return { questions: arr };
    } catch (_) {}
  }

  throw new Error("Could not parse valid questions JSON: " + cleaned.substring(0, 150));
}

async function callGeminiForQuestions(
  apiKey: string,
  materialTitle: string,
  text: string,
  mode: string,
  type: string,
  difficulty: string,
  count: number
): Promise<{ questions: unknown[] }> {
  const truncatedText = text.length > MAX_INPUT_TEXT_LENGTH
    ? text.substring(0, MAX_INPUT_TEXT_LENGTH) + "\n\n[Text truncated due to length limits.]"
    : text;

  let questionSchema = "";
  let exampleQuestion = "";

  if (mode === "QUIZ" || type === "MCQ") {
    questionSchema = `{"questions":[{"id":"q1","text":"<clear question statement>","options":["<option A>","<option B>","<option C>","<option D>"],"correctOptionIndex":0,"keyConcepts":["<concept 1>","<concept 2>"]}]}`;
    exampleQuestion = `Example: {"id":"q1","text":"What is the primary function of an operating system kernel?","options":["Managing hardware resources and memory","Compiling high-level programming code","Providing internet connectivity","Designing graphical user interfaces"],"correctOptionIndex":0,"keyConcepts":["kernel","operating system"]}`;
  } else if (type === "Viva") {
    questionSchema = `{"questions":[{"id":"v1","text":"<viva question>","expectedPoints":["<key point 1>","<key point 2>","<key point 3>"],"keyConcepts":["<concept 1>","<concept 2>"]}]}`;
    exampleQuestion = `Example: {"id":"v1","text":"Explain how virtual memory operates and its benefits.","expectedPoints":["Translates virtual addresses to physical RAM addresses","Uses paging and swap space on disk","Allows processes to exceed physical memory size"],"keyConcepts":["virtual memory","paging"]}`;
  } else {
    questionSchema = `{"questions":[{"id":"l1","text":"<analytical question>","expectedPoints":["<point 1>","<point 2>","<point 3>"],"keyConcepts":["<concept 1>","<concept 2>"]}]}`;
    exampleQuestion = `Example: {"id":"l1","text":"Discuss the architecture and tradeoffs of microkernel vs monolithic kernel designs.","expectedPoints":["Monolithic kernels run all core services in supervisor mode for performance","Microkernels isolate services in user space for modularity and fault tolerance","Tradeoffs include IPC overhead vs crash vulnerability"],"keyConcepts":["microkernel","monolithic kernel"]}`;
  }

  const prompt = `You are a university professor creating rigorous, document-specific exam questions.

TASK: Generate exactly ${count} ${type === "MCQ" || mode === "QUIZ" ? "multiple-choice (MCQ)" : type} questions of ${difficulty} difficulty based STRICTLY on the document provided below.

STRICT RULES:
1. Every question MUST be directly answerable from the provided SOURCE TEXT only.
2. Do NOT invent facts, numbers, equations, or concepts not explicitly stated in the document.
3. For MCQ: Provide exactly 4 options. Only ONE must be correct according to the text. The remaining 3 must be plausible distractors from related topics in the document.
4. For MCQ: correctOptionIndex must be an integer (0, 1, 2, or 3) corresponding to the correct entry in the options array.
5. Return ONLY a valid JSON object matching the schema below. No markdown fences, no conversational text.

DIFFICULTY LEVEL: ${difficulty}

JSON SCHEMA:
${questionSchema}

${exampleQuestion}

DOCUMENT TITLE: ${materialTitle}

SOURCE TEXT:
${truncatedText}

OUTPUT JSON:`;

  const availableModels = await getAvailableGeminiModels(apiKey);
  console.log(`Discovered available models: ${availableModels.join(", ")}`);

  const modelsToTry = [
    ...availableModels,
    "gemini-2.0-flash",
    "gemini-1.5-flash",
    "gemini-1.5-flash-latest",
  ].filter((v, i, a) => a.indexOf(v) === i);

  const errors: string[] = [];

  for (const model of modelsToTry) {
    const MAX_RETRIES = 3;
    for (let attempt = 1; attempt <= MAX_RETRIES; attempt++) {
      try {
        console.log(`Attempting model=${model} attempt=${attempt}/${MAX_RETRIES}`);
        const url = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent?key=${apiKey}`;

        const reqBody = {
          contents: [{ role: "user", parts: [{ text: prompt }] }],
          generationConfig: {
            temperature: 0.3,
            maxOutputTokens: 8192,
            responseMimeType: "application/json",
          },
          safetySettings: [
            { category: "HARM_CATEGORY_HARASSMENT", threshold: "BLOCK_NONE" },
            { category: "HARM_CATEGORY_HATE_SPEECH", threshold: "BLOCK_NONE" },
            { category: "HARM_CATEGORY_SEXUALLY_EXPLICIT", threshold: "BLOCK_NONE" },
            { category: "HARM_CATEGORY_DANGEROUS_CONTENT", threshold: "BLOCK_NONE" },
          ],
        };

        const res = await fetch(url, {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(reqBody),
        });

        if (res.status === 404) {
          console.warn(`Model ${model} returned 404. Skipping.`);
          errors.push(`${model}: 404 Not Found`);
          break; // Try next model immediately
        }

        if (res.status === 503 || res.status === 429) {
          const errBody = await res.text();
          console.warn(`Model ${model} overloaded/rate-limited (${res.status}): ${errBody.substring(0, 100)}`);
          errors.push(`${model} (attempt ${attempt}): ${res.status}`);
          if (attempt < MAX_RETRIES) {
            await new Promise((r) => setTimeout(r, attempt * 1500));
            continue;
          }
          break; // Retries exhausted, move to next model
        }

        if (!res.ok) {
          const errBody = await res.text();
          // If responseMimeType is not supported, retry without it
          if (errBody.includes("responseMimeType") || res.status === 400) {
            console.warn(`Model ${model} failed with responseMimeType, trying without it...`);
            delete (reqBody.generationConfig as Record<string, unknown>).responseMimeType;
            const resFallback = await fetch(url, {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify(reqBody),
            });
            if (resFallback.ok) {
              const fallbackData = await resFallback.json();
              const candidate = fallbackData?.candidates?.[0];
              const textContent = extractCandidateText(candidate);
              if (textContent) {
                const parsed = parseQuizJson(textContent);
                console.log(`Success with model ${model} (without responseMimeType)`);
                return parsed;
              }
            }
          }
          errors.push(`${model}: HTTP ${res.status} - ${errBody.substring(0, 100)}`);
          break;
        }

        const data = await res.json();
        const candidate = data?.candidates?.[0];
        if (!candidate) {
          const blockReason = data?.promptFeedback?.blockReason;
          errors.push(`${model}: prompt blocked (${blockReason ?? "no candidates"})`);
          break;
        }

        const textContent = extractCandidateText(candidate);
        if (!textContent) {
          const finishReason = candidate.finishReason ?? "unknown";
          errors.push(`${model}: empty output (${finishReason})`);
          break;
        }

        const parsed = parseQuizJson(textContent);
        console.log(`Successfully generated ${parsed.questions.length} questions using ${model}`);
        return parsed;
      } catch (err) {
        const msg = err instanceof Error ? err.message : String(err);
        console.warn(`Error on ${model} (attempt ${attempt}): ${msg}`);
        errors.push(`${model}: ${msg}`);
        if (attempt < MAX_RETRIES) {
          await new Promise((r) => setTimeout(r, 1000));
        }
      }
    }
  }

  throw new Error(`All Gemini models failed. Diagnostics: [${errors.join("; ")}]`);
}

serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response(null, {
      status: 204,
      headers: {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Methods": "POST, OPTIONS",
        "Access-Control-Allow-Headers": "Authorization, apikey, Content-Type",
      },
    });
  }

  if (req.method !== "POST") {
    return new Response(JSON.stringify({ error: "Method not allowed. Use POST." }), {
      status: 405,
      headers: { "Content-Type": "application/json" },
    });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader?.startsWith("Bearer ")) {
      return new Response(JSON.stringify({ error: "Missing Authorization header." }), {
        status: 401,
        headers: { "Content-Type": "application/json" },
      });
    }

    const apiKey = req.headers.get("apikey");
    if (!apiKey) {
      return new Response(JSON.stringify({ error: "Missing apikey header." }), {
        status: 401,
        headers: { "Content-Type": "application/json" },
      });
    }

    const body = await req.json();
    const { materialTitle, text, mode, type, difficulty, count } = body;

    if (!text || typeof text !== "string" || text.trim().length < 30) {
      return new Response(
        JSON.stringify({ error: "Extracted document text is too short to generate quality questions." }),
        { status: 400, headers: { "Content-Type": "application/json" } }
      );
    }

    const geminiApiKey = Deno.env.get("GEMINI_API_KEY");
    if (!geminiApiKey) {
      return new Response(
        JSON.stringify({ error: "AI service not configured on server (missing GEMINI_API_KEY secret)." }),
        { status: 500, headers: { "Content-Type": "application/json" } }
      );
    }

    const questionCount = Math.min(Math.max(Number(count) || 5, 1), 30);
    console.log(
      `Request received: title="${materialTitle}" mode=${mode} type=${type} difficulty=${difficulty} count=${questionCount} textLength=${text.length}`
    );

    const result = await callGeminiForQuestions(
      geminiApiKey,
      materialTitle || "Study Document",
      text,
      mode || "QUESTIONS",
      type || "MCQ",
      difficulty || "Medium",
      questionCount
    );

    return new Response(JSON.stringify(result), {
      status: 200,
      headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
    });
  } catch (err) {
    const message = err instanceof Error ? err.message : "Internal server error.";
    console.error("generate-quiz execution error:", message);
    return new Response(JSON.stringify({ error: message }), {
      status: 500,
      headers: { "Content-Type": "application/json", "Access-Control-Allow-Origin": "*" },
    });
  }
});
