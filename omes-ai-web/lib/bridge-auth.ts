const STORAGE_KEY = "omes_ai_bridge_ctx";

type BridgeContext = Record<string, string>;

export async function bootstrapBridgeContextFromUrl(): Promise<void> {
  if (typeof window === "undefined") return;
  const current = new URL(window.location.href);
  const ticket = current.searchParams.get("bridge_ticket");
  if (!ticket) return;
  try {
    const res = await fetch(`/api/bridge/resolve?ticket=${encodeURIComponent(ticket)}`, {
      method: "GET",
      cache: "no-store",
      credentials: "include"
    });
    if (!res.ok) {
      throw new Error(`resolve failed: ${res.status}`);
    }
    const data = (await res.json()) as BridgeContext;
    window.sessionStorage.setItem(STORAGE_KEY, JSON.stringify(data || {}));
  } catch (_err) {
    // ignore bridge parse errors
  } finally {
    current.searchParams.delete("bridge_ticket");
    window.history.replaceState({}, "", current.toString());
  }
}

export function getBridgeHeaders(): BridgeContext {
  if (typeof window === "undefined") return {};
  try {
    const raw = window.sessionStorage.getItem(STORAGE_KEY);
    if (!raw) return {};
    return JSON.parse(raw) as BridgeContext;
  } catch (_err) {
    return {};
  }
}
