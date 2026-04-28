const PORTAL_BASE_URL =
  process.env.OMES_PORTAL_BASE_URL ??
  "http://127.0.0.1:10010";
const INTERNAL_KEY =
  process.env.BRIDGE_INTERNAL_KEY ??
  "omes-bridge-internal";

type PortalJsonResponse<T> = {
  code: number;
  message?: string;
  msg?: string;
  data: T;
};

export async function GET(req: Request): Promise<Response> {
  const url = new URL(req.url);
  const ticket = (url.searchParams.get("ticket") || "").trim();
  if (!ticket) {
    return new Response(JSON.stringify({ message: "missing ticket" }), { status: 400 });
  }
  const resolveUrl = `${PORTAL_BASE_URL}/open/ai/bridge/resolve?ticket=${encodeURIComponent(ticket)}`;
  const res = await fetch(resolveUrl, {
    method: "GET",
    headers: {
      "x-bridge-key": INTERNAL_KEY
    },
    cache: "no-store"
  });
  const text = await res.text();
  if (!res.ok) {
    return new Response(text || JSON.stringify({ message: "bridge resolve failed" }), { status: res.status });
  }
  let json: PortalJsonResponse<Record<string, string>>;
  try {
    json = JSON.parse(text) as PortalJsonResponse<Record<string, string>>;
  } catch (_err) {
    return new Response(JSON.stringify({ message: "invalid bridge response" }), { status: 502 });
  }
  if (json.code !== 200 || !json.data) {
    return new Response(JSON.stringify({ message: json.message || json.msg || "bridge resolve failed" }), { status: 401 });
  }
  return new Response(JSON.stringify(json.data), {
    status: 200,
    headers: {
      "Content-Type": "application/json; charset=utf-8"
    }
  });
}
