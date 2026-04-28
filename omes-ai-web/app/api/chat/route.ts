import type { UIMessage } from "ai";

const BASE_URL =
  process.env.NEXT_PUBLIC_OMES_AI_BASE_URL ??
  process.env.OMES_AI_BASE_URL ??
  "http://127.0.0.1:10011";
const STREAM_URL = `${BASE_URL}/inspection/ai/agent/multi-chat/stream`;

type IncomingBody = {
  messages?: UIMessage[];
  sessionId?: string;
  history?: string[];
  selectedAgents?: string[];
};

function buildForwardHeaders(req: Request): Record<string, string> {
  const headers: Record<string, string> = {
    "Content-Type": "application/json"
  };
  const allowExact = new Set([
    "authorization",
    "cookie",
    "token",
    "x-token",
    "x-access-token",
    "access-token",
    "era-token",
    "x-era-token",
    "origin",
    "referer"
  ]);
  for (const [key, value] of req.headers.entries()) {
    const lower = key.toLowerCase();
    if (allowExact.has(lower) || lower.startsWith("x-")) {
      headers[key] = value;
    }
  }
  return headers;
}

function extractMessageText(message: UIMessage | undefined): string {
  if (!message) return "";
  if (typeof message.content === "string") {
    return message.content;
  }
  return (message.parts ?? [])
    .filter((part): part is Extract<(typeof message.parts)[number], { type: "text" }> => part.type === "text")
    .map((part) => part.text)
    .join("\n");
}

export async function POST(req: Request): Promise<Response> {
  const body = (await req.json()) as IncomingBody;
  const lastMessage = body.messages?.[body.messages.length - 1];
  const userMessage = extractMessageText(lastMessage);

  const backendPayload = {
    message: userMessage,
    sessionId: body.sessionId || "",
    history: body.history ?? [],
    selectedAgents: body.selectedAgents ?? []
  };

  const headers = buildForwardHeaders(req);

  const backendResponse = await fetch(STREAM_URL, {
    method: "POST",
    headers,
    body: JSON.stringify(backendPayload),
    cache: "no-store"
  });

  if (!backendResponse.ok || !backendResponse.body) {
    const detail = await backendResponse.text().catch(() => "");
    return new Response(detail || "backend stream failed", { status: backendResponse.status || 500 });
  }

  const encoder = new TextEncoder();
  const decoder = new TextDecoder();

  const stream = new ReadableStream<Uint8Array>({
    async start(controller) {
      const reader = backendResponse.body!.getReader();
      let buffer = "";
      let eventName = "";
      let eventData = "";

      const flushEvent = () => {
        if (!eventName) {
          eventData = "";
          return;
        }
        try {
          const parsed = eventData ? JSON.parse(eventData) : {};
          if (eventName === "node_reply") {
            const title = `[${parsed.agentName ?? "agent"} | ${parsed.role ?? "-"}]\n`;
            controller.enqueue(encoder.encode(`${title}${parsed.content ?? ""}\n\n`));
          } else if (eventName === "final") {
            controller.enqueue(encoder.encode(`[final]\n${parsed.finalAnswer ?? ""}`));
          } else if (eventName === "error") {
            controller.enqueue(encoder.encode(`\n[error] ${parsed.message ?? "stream error"}`));
          }
        } catch (_err) {
          controller.enqueue(encoder.encode("\n[error] invalid event payload"));
        } finally {
          eventName = "";
          eventData = "";
        }
      };

      while (true) {
        const { done, value } = await reader.read();
        if (done) {
          flushEvent();
          controller.close();
          break;
        }
        buffer += decoder.decode(value, { stream: true });
        const lines = buffer.split("\n");
        buffer = lines.pop() ?? "";
        for (const rawLine of lines) {
          const line = rawLine.trimEnd();
          if (!line) {
            flushEvent();
            continue;
          }
          if (line.startsWith("event:")) {
            eventName = line.slice(6).trim();
          } else if (line.startsWith("data:")) {
            eventData += line.slice(5).trim();
          }
        }
      }
    }
  });

  return new Response(stream, {
    headers: {
      "Content-Type": "text/plain; charset=utf-8"
    }
  });
}
