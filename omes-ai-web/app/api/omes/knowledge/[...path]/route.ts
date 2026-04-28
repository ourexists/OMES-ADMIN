const BASE_URL =
  process.env.OMES_AI_BASE_URL ??
  process.env.NEXT_PUBLIC_OMES_AI_BASE_URL ??
  "http://127.0.0.1:10011";
const KNOWLEDGE_PREFIX = `${BASE_URL}/inspection/ai/knowledge`;

type RouteContext = {
  params: {
    path: string[];
  };
};

function buildForwardHeaders(req: Request, hasBody: boolean): Record<string, string> {
  const headers: Record<string, string> = {
    ...(hasBody ? { "Content-Type": "application/json" } : {})
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

function buildTargetUrl(path: string[], requestUrl: string): string {
  const incoming = new URL(requestUrl);
  const target = new URL(`${KNOWLEDGE_PREFIX}/${path.join("/")}`);
  target.search = incoming.search;
  return target.toString();
}

async function forward(method: string, req: Request, context: RouteContext): Promise<Response> {
  const hasBody = method !== "GET";
  const backendResponse = await fetch(buildTargetUrl(context.params.path, req.url), {
    method,
    headers: buildForwardHeaders(req, hasBody),
    body: hasBody ? await req.text() : undefined,
    cache: "no-store"
  });
  const text = await backendResponse.text();
  return new Response(text, {
    status: backendResponse.status,
    headers: {
      "Content-Type": backendResponse.headers.get("content-type") ?? "application/json; charset=utf-8"
    }
  });
}

export async function GET(req: Request, context: RouteContext): Promise<Response> {
  return forward("GET", req, context);
}

export async function POST(req: Request, context: RouteContext): Promise<Response> {
  return forward("POST", req, context);
}
