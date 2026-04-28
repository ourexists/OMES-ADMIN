"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import styles from "./page.module.css";
import { askKnowledge, ingestKnowledge, reindexKnowledge } from "@/lib/api";
import { bootstrapBridgeContextFromUrl } from "@/lib/bridge-auth";
import type { KnowledgeType } from "@/lib/types";

export default function KnowledgePage() {
  const [reindexLimit, setReindexLimit] = useState("50");
  const [reindexResult, setReindexResult] = useState("");

  const [knowledgeType, setKnowledgeType] = useState<KnowledgeType>("unstructured");
  const [sourceName, setSourceName] = useState("");
  const [textContent, setTextContent] = useState("");
  const [structuredContent, setStructuredContent] = useState("");
  const [ingestResult, setIngestResult] = useState("");

  const [question, setQuestion] = useState("");
  const [topK, setTopK] = useState("5");
  const [askResult, setAskResult] = useState("");

  const [loading, setLoading] = useState<"" | "reindex" | "ingest" | "ask">("");
  const [errorText, setErrorText] = useState("");
  const [bridgeReady, setBridgeReady] = useState(false);

  useEffect(() => {
    void (async () => {
      await bootstrapBridgeContextFromUrl();
      setBridgeReady(true);
    })();
  }, []);

  const structuredMode = knowledgeType === "structured";
  const canIngest = useMemo(() => {
    if (structuredMode) {
      return structuredContent.trim().length > 0;
    }
    return textContent.trim().length > 0;
  }, [structuredContent, structuredMode, textContent]);

  const onReindex = async () => {
    setErrorText("");
    setLoading("reindex");
    try {
      const limit = Number.parseInt(reindexLimit, 10);
      const data = await reindexKnowledge(Number.isNaN(limit) ? undefined : limit);
      setReindexResult(`重建完成：${data.indexed} 条，provider=${data.provider}`);
    } catch (err) {
      setErrorText(err instanceof Error ? err.message : "重建失败");
    } finally {
      setLoading("");
    }
  };

  const onIngest = async () => {
    if (!canIngest) return;
    setErrorText("");
    setLoading("ingest");
    try {
      const data = await ingestKnowledge({
        knowledgeType,
        sourceName: sourceName.trim() || undefined,
        textContent: structuredMode ? undefined : textContent,
        structuredContent: structuredMode ? structuredContent : undefined
      });
      setIngestResult(`入库完成：${data.ingested} 条，provider=${data.provider}`);
    } catch (err) {
      setErrorText(err instanceof Error ? err.message : "入库失败");
    } finally {
      setLoading("");
    }
  };

  const onAsk = async () => {
    if (!question.trim()) return;
    setErrorText("");
    setLoading("ask");
    try {
      const parsedTopK = Number.parseInt(topK, 10);
      const data = await askKnowledge(question, Number.isNaN(parsedTopK) ? undefined : parsedTopK);
      setAskResult(data.answer || "(无返回)");
    } catch (err) {
      setErrorText(err instanceof Error ? err.message : "检索失败");
    } finally {
      setLoading("");
    }
  };

  return (
    <main className={styles.layout}>
      <div className={styles.header}>
        <h1>知识库管理</h1>
        <Link href="/" className={styles.linkBtn}>
          返回聊天
        </Link>
      </div>

      {errorText ? <div className={styles.error}>错误：{errorText}</div> : null}

      <section className={styles.card}>
        <h2>1. 重建巡检知识库</h2>
        <div className={styles.row}>
          <input
            className={styles.input}
            value={reindexLimit}
            onChange={(e) => setReindexLimit(e.target.value)}
            placeholder="limit，默认 50"
          />
          <button className={styles.btn} onClick={() => void onReindex()} disabled={loading !== "" || !bridgeReady}>
            {loading === "reindex" ? "执行中..." : "重建"}
          </button>
        </div>
        {reindexResult ? <div className={styles.result}>{reindexResult}</div> : null}
        {!bridgeReady ? <div className={styles.result}>正在初始化桥接鉴权上下文...</div> : null}
      </section>

      <section className={styles.card}>
        <h2>2. 知识入库</h2>
        <div className={styles.row}>
          <select
            className={styles.input}
            value={knowledgeType}
            onChange={(e) => setKnowledgeType(e.target.value as KnowledgeType)}
          >
            <option value="unstructured">非结构化</option>
            <option value="structured">结构化(JSON)</option>
          </select>
          <input
            className={styles.input}
            value={sourceName}
            onChange={(e) => setSourceName(e.target.value)}
            placeholder="来源名称（可选）"
          />
        </div>
        <textarea
          className={styles.textarea}
          value={structuredMode ? structuredContent : textContent}
          onChange={(e) => (structuredMode ? setStructuredContent(e.target.value) : setTextContent(e.target.value))}
          placeholder={
            structuredMode
              ? "输入 JSON 对象或数组"
              : "输入非结构化文本，空行会自动拆段入库"
          }
        />
        <div className={styles.row}>
          <button
            className={styles.btn}
            onClick={() => void onIngest()}
            disabled={loading !== "" || !canIngest || !bridgeReady}
          >
            {loading === "ingest" ? "入库中..." : "执行入库"}
          </button>
        </div>
        {ingestResult ? <div className={styles.result}>{ingestResult}</div> : null}
      </section>

      <section className={styles.card}>
        <h2>3. 知识检索问答</h2>
        <div className={styles.row}>
          <input
            className={styles.input}
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            placeholder="输入问题"
          />
          <input
            className={styles.input}
            value={topK}
            onChange={(e) => setTopK(e.target.value)}
            placeholder="topK"
          />
          <button
            className={styles.btn}
            onClick={() => void onAsk()}
            disabled={loading !== "" || !question.trim() || !bridgeReady}
          >
            {loading === "ask" ? "检索中..." : "检索"}
          </button>
        </div>
        {askResult ? <pre className={styles.answer}>{askResult}</pre> : null}
      </section>
    </main>
  );
}
