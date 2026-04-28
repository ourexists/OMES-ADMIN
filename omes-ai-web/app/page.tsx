"use client";

import { useEffect, useRef, useState } from "react";
import type { UIMessage } from "ai";
import { useChat } from "@ai-sdk/react";
import Link from "next/link";
import styles from "./page.module.css";
import {
  createSession,
  deleteSession,
  fetchConfig,
  fetchMessages,
  fetchSessions
} from "@/lib/api";
import { bootstrapBridgeContextFromUrl, getBridgeHeaders } from "@/lib/bridge-auth";
import type { MultiAgentConfig, SessionDto } from "@/lib/types";

const defaultConfig: MultiAgentConfig = {
  maxHistory: 20,
  maxMessageLength: 2000,
  enabledAgents: ["dispatch-agent"]
};

function toUiMessage(role: string, content: string): UIMessage {
  return {
    id: crypto.randomUUID(),
    role: role as UIMessage["role"],
    content,
    parts: [{ type: "text", text: content }]
  };
}

function flattenText(parts: UIMessage["parts"]): string {
  return parts
    .filter((part): part is Extract<(typeof parts)[number], { type: "text" }> => part.type === "text")
    .map((part) => part.text)
    .join("\n");
}

function parseAssistantBlocks(text: string): Array<{ title: string; content: string; kind: string }> {
  const source = (text || "").trim();
  if (!source) return [];
  const lines = source.split("\n");
  const blocks: Array<{ title: string; content: string; kind: string }> = [];
  let currentTitle = "assistant";
  let currentKind = "assistant";
  let currentContent: string[] = [];

  const pushBlock = () => {
    if (currentContent.length === 0) return;
    blocks.push({ title: currentTitle, content: currentContent.join("\n").trim(), kind: currentKind });
  };

  for (const line of lines) {
    const nodeMatch = line.match(/^\[(.+?)\s\|\s(.+?)\]$/);
    if (nodeMatch) {
      pushBlock();
      currentTitle = `${nodeMatch[1]} | ${nodeMatch[2]}`;
      currentKind = String(nodeMatch[1]).toLowerCase();
      currentContent = [];
      continue;
    }
    if (/^\[final\]$/.test(line)) {
      pushBlock();
      currentTitle = "final";
      currentKind = "final";
      currentContent = [];
      continue;
    }
    currentContent.push(line);
  }
  pushBlock();
  return blocks.length > 0 ? blocks : [{ title: "assistant", content: source, kind: "assistant" }];
}

function cardClassByKind(kind: string): string {
  if (kind.includes("dispatch")) return styles.cardPlanner;
  if (kind.includes("device")) return styles.cardInspection;
  if (kind.includes("knowledge")) return styles.cardKnowledge;
  if (kind === "final") return styles.cardFinal;
  return styles.cardDefault;
}

function severityClassByContent(kind: string, content: string): string {
  if (kind !== "final") return "";
  const text = (content || "").toUpperCase();
  if (text.includes("P1") || text.includes("一级") || text.includes("紧急")) {
    return styles.severityP1;
  }
  if (text.includes("P2") || text.includes("二级") || text.includes("高")) {
    return styles.severityP2;
  }
  if (text.includes("P3") || text.includes("三级") || text.includes("中")) {
    return styles.severityP3;
  }
  return "";
}

function severityBadgeLabel(kind: string, content: string): string {
  if (kind !== "final") return "";
  const text = (content || "").toUpperCase();
  if (text.includes("P1") || text.includes("一级") || text.includes("紧急")) return "P1";
  if (text.includes("P2") || text.includes("二级") || text.includes("高")) return "P2";
  if (text.includes("P3") || text.includes("三级") || text.includes("中")) return "P3";
  return "";
}

function severityBadgeClass(kind: string, content: string): string {
  const label = severityBadgeLabel(kind, content);
  if (label === "P1") return styles.badgeP1;
  if (label === "P2") return styles.badgeP2;
  if (label === "P3") return styles.badgeP3;
  return "";
}

export default function Page() {
  const [bridgeHeaders, setBridgeHeaders] = useState<Record<string, string>>({});
  const [config, setConfig] = useState<MultiAgentConfig>(defaultConfig);
  const [sessions, setSessions] = useState<SessionDto[]>([]);
  const [sessionId, setSessionId] = useState("");
  const [errorText, setErrorText] = useState("");
  const chatRef = useRef<HTMLDivElement>(null);
  const {
    messages,
    setMessages,
    input,
    setInput,
    append,
    stop,
    reload,
    status,
    error
  } = useChat({
    api: "/api/chat",
    streamProtocol: "text",
    headers: bridgeHeaders
  });
  const sending = status === "submitted" || status === "streaming";

  useEffect(() => {
    void (async () => {
      await bootstrapBridgeContextFromUrl();
      setBridgeHeaders(getBridgeHeaders());
    })();
  }, []);

  useEffect(() => {
    void (async () => {
      const [cfg, list] = await Promise.all([fetchConfig(), fetchSessions()]);
      setConfig(cfg);
      setSessions(list);
      if (list.length > 0) {
        setSessionId(list[0].sessionId);
      }
    })();
  }, []);

  useEffect(() => {
    if (!sessionId) return;
    void (async () => {
      const list = await fetchMessages(sessionId);
      setMessages(
        list.map((item) =>
          toUiMessage(item.role.toLowerCase() === "user" ? "user" : "assistant", `[${item.role}] ${item.content}`)
        )
      );
    })();
  }, [sessionId, setMessages]);

  useEffect(() => {
    if (chatRef.current) {
      chatRef.current.scrollTop = chatRef.current.scrollHeight;
    }
  }, [messages, sending]);

  useEffect(() => {
    if (error) {
      setErrorText(error.message || "请求失败");
    }
  }, [error]);

  const onCreateSession = async () => {
    const newId = await createSession("新对话");
    setSessionId(newId);
    setMessages([]);
    const list = await fetchSessions();
    setSessions(list);
  };

  const onDeleteSession = async (targetSessionId: string) => {
    if (!targetSessionId) return;
    const ok = window.confirm("确认删除该会话吗？删除后不可恢复。");
    if (!ok) return;
    const deleted = await deleteSession(targetSessionId);
    if (!deleted) {
      setErrorText("删除失败，会话可能不存在");
      return;
    }
    const list = await fetchSessions();
    setSessions(list);
    if (sessionId === targetSessionId) {
      const nextSessionId = list.length > 0 ? list[0].sessionId : "";
      setSessionId(nextSessionId);
      if (!nextSessionId) {
        setMessages([]);
      }
    }
  };

  const onSend = async () => {
    const text = input.trim();
    if (!text || sending) return;
    if (text.length > config.maxMessageLength) {
      setErrorText(`消息超过长度限制（${config.maxMessageLength}）`);
      return;
    }
    setErrorText("");
    setInput("");
    let requestSessionId = sessionId;
    if (!requestSessionId) {
      requestSessionId = await createSession("新对话");
      setSessionId(requestSessionId);
    }
    const history = messages.slice(-config.maxHistory).map((msg) => `${msg.role}: ${flattenText(msg.parts)}`);
    try {
      await append(
        {
          role: "user",
          content: text
        },
        {
          body: {
            sessionId: requestSessionId,
            history,
            selectedAgents: ["dispatch-agent"]
          }
        }
      );
    } catch (err) {
      setErrorText(err instanceof Error ? err.message : "发送失败");
    } finally {
      const list = await fetchSessions();
      setSessions(list);
    }
  };

  return (
    <main className={styles.layout}>
      <section className={`${styles.panel} ${styles.sidebar}`}>
        <div className={styles.header}>
          <span>会话列表</span>
          <button className={styles.btn} type="button" onClick={onCreateSession}>
            新建
          </button>
        </div>
        <div className={styles.sessionList}>
          {sessions.map((item) => (
            <div
              key={item.sessionId}
              className={`${styles.sessionItem} ${sessionId === item.sessionId ? styles.active : ""}`}
              onClick={() => setSessionId(item.sessionId)}
              role="button"
              tabIndex={0}
            >
              <div className={styles.sessionTitleRow}>
                <span className={styles.sessionTitleText}>{item.title || "新对话"}</span>
                <button
                  className={styles.deleteBtn}
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation();
                    void onDeleteSession(item.sessionId);
                  }}
                >
                  删除
                </button>
              </div>
              <div className={styles.hint}>{item.sessionId}</div>
            </div>
          ))}
        </div>
      </section>

      <section className={`${styles.panel} ${styles.main}`}>
        <div className={styles.header}>
          <Link className={styles.btn} href="/knowledge">
            知识库管理
          </Link>
          <div className={styles.actions}>
            <button className={styles.btn} type="button" onClick={() => stop()} disabled={!sending}>
              停止
            </button>
            <button
              className={styles.btn}
              type="button"
              onClick={() => void reload()}
              disabled={sending || messages.length === 0}
            >
              重试
            </button>
          </div>
        </div>

        <div className={styles.chat} ref={chatRef}>
          {messages.map((msg) => (
            <div
              key={msg.id}
              className={`${styles.msg} ${msg.role === "user" ? styles.msgUser : ""}`}
            >
              <div className={styles.bubble}>
                <div className={styles.role}>{msg.role}</div>
                {msg.role === "assistant" ? (
                  <div className={styles.assistantBlocks}>
                    {parseAssistantBlocks(flattenText(msg.parts)).map((block, idx) => (
                      <div
                        key={`${msg.id}-${idx}`}
                        className={`${styles.assistantCard} ${cardClassByKind(block.kind)} ${severityClassByContent(block.kind, block.content)}`}
                      >
                        <div className={styles.assistantCardTitle}>
                          <span>{block.title}</span>
                          {severityBadgeLabel(block.kind, block.content) ? (
                            <span className={`${styles.badge} ${severityBadgeClass(block.kind, block.content)}`}>
                              {severityBadgeLabel(block.kind, block.content)}
                            </span>
                          ) : null}
                        </div>
                        <div>{block.content}</div>
                      </div>
                    ))}
                  </div>
                ) : (
                  <div>{flattenText(msg.parts)}</div>
                )}
              </div>
            </div>
          ))}
          {sending ? <div className={styles.hint}>正在处理中...</div> : null}
        </div>

        <div className={styles.composer}>
          <div className={styles.row}>
            <input
              className={styles.input}
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="输入你的工业巡检问题"
              onKeyDown={(e) => {
                if (e.key === "Enter") {
                  void onSend();
                }
              }}
            />
            <button className={styles.btn} type="button" onClick={() => void onSend()} disabled={sending}>
              发送
            </button>
          </div>
          <div>
            {errorText ? <div className={styles.hint}>错误: {errorText}</div> : null}
          </div>
        </div>
      </section>
    </main>
  );
}
