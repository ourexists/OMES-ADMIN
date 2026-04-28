import type {
  AgentNodeReply,
  KnowledgeAskResponse,
  KnowledgeIngestRequest,
  KnowledgeIngestResponse,
  KnowledgeReindexResponse,
  MultiAgentConfig,
  SessionDto,
  SessionMessageDto
} from "@/lib/types";
import { getBridgeHeaders } from "@/lib/bridge-auth";

const API_PREFIX = "/api/omes/agent";

type JsonResponse<T> = {
  code: number;
  message: string;
  data: T;
};

async function request<T>(url: string, init?: RequestInit): Promise<T> {
  const method = (init?.method || "GET").toUpperCase();
  const bridgeHeaders = getBridgeHeaders();
  const res = await fetch(url, {
    ...init,
    headers: {
      ...(method !== "GET" ? { "Content-Type": "application/json" } : {}),
      ...bridgeHeaders,
      ...(init?.headers ?? {})
    },
    credentials: "include",
    cache: "no-store"
  });
  if (!res.ok) {
    const raw = await res.text().catch(() => "");
    let backendMessage = raw;
    try {
      const json = raw ? (JSON.parse(raw) as Partial<JsonResponse<unknown>>) : null;
      if (json && typeof json.message === "string" && json.message.trim()) {
        backendMessage = json.message;
      }
    } catch (_err) {
      // Keep raw text when payload is not JSON.
    }
    const detail = backendMessage ? ` - ${backendMessage}` : "";
    throw new Error(`HTTP ${res.status}${detail}`);
  }
  const json = (await res.json().catch(() => null)) as JsonResponse<T> | null;
  if (!json) {
    throw new Error("invalid response payload");
  }
  if (json.code !== 200) {
    throw new Error(json.message || "request failed");
  }
  return json.data;
}

export async function fetchConfig(): Promise<MultiAgentConfig> {
  return request<MultiAgentConfig>(`${API_PREFIX}/multi-chat/config`);
}

export async function fetchSessions(): Promise<SessionDto[]> {
  return request<SessionDto[]>(`${API_PREFIX}/multi-chat/session/list`);
}

export async function createSession(title?: string): Promise<string> {
  const data = await request<{ sessionId: string }>(
    `${API_PREFIX}/multi-chat/session/create`,
    {
      method: "POST",
      body: JSON.stringify({ title: title || "新对话" })
    }
  );
  return data.sessionId;
}

export async function deleteSession(sessionId: string): Promise<boolean> {
  const data = await request<{ deleted: boolean }>(
    `${API_PREFIX}/multi-chat/session/delete?sessionId=${encodeURIComponent(sessionId)}`,
    {
      method: "POST"
    }
  );
  return Boolean(data.deleted);
}

export async function fetchMessages(sessionId: string): Promise<SessionMessageDto[]> {
  return request<SessionMessageDto[]>(
    `${API_PREFIX}/multi-chat/session/messages?sessionId=${encodeURIComponent(sessionId)}`
  );
}

export async function reindexKnowledge(limit?: number): Promise<KnowledgeReindexResponse> {
  return request<KnowledgeReindexResponse>(`/api/omes/knowledge/reindex`, {
    method: "POST",
    body: JSON.stringify({ limit: limit && limit > 0 ? limit : undefined })
  });
}

export async function ingestKnowledge(payload: KnowledgeIngestRequest): Promise<KnowledgeIngestResponse> {
  return request<KnowledgeIngestResponse>(`/api/omes/knowledge/ingest`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export async function askKnowledge(question: string, topK?: number): Promise<KnowledgeAskResponse> {
  return request<KnowledgeAskResponse>(`/api/omes/knowledge/ask`, {
    method: "POST",
    body: JSON.stringify({
      question,
      topK: topK && topK > 0 ? topK : undefined
    })
  });
}

export type StreamPayload = {
  message: string;
  sessionId?: string;
  history: string[];
  selectedAgents: string[];
};

export type StreamHandlers = {
  onSession: (sessionId: string) => void;
  onNodeReply: (reply: AgentNodeReply) => void;
  onFinal: (finalText: string) => void;
  onError: (message: string) => void;
};

export async function streamMultiAgentChat(
  payload: StreamPayload,
  handlers: StreamHandlers
): Promise<void> {
  const response = await fetch(`/api/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload)
  });
  if (!response.ok || !response.body) {
    throw new Error(`SSE connect failed: ${response.status}`);
  }

  const reader = response.body.getReader();
  const decoder = new TextDecoder();
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
      if (eventName === "session" && parsed.sessionId) {
        handlers.onSession(parsed.sessionId);
      } else if (eventName === "node_reply") {
        handlers.onNodeReply(parsed as AgentNodeReply);
      } else if (eventName === "final") {
        handlers.onFinal(parsed.finalAnswer ?? "");
      } else if (eventName === "error") {
        handlers.onError(parsed.message ?? "stream error");
      }
    } catch (_err) {
      handlers.onError("invalid SSE payload");
    } finally {
      eventName = "";
      eventData = "";
    }
  };

  while (true) {
    const { done, value } = await reader.read();
    if (done) {
      flushEvent();
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
