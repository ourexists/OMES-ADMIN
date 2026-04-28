import type { UIMessage } from "ai";

export type AgentNodeReply = {
  agentName: string;
  role: string;
  content: string;
};

export type MultiAgentConfig = {
  maxHistory: number;
  maxMessageLength: number;
  enabledAgents: string[];
};

export type SessionDto = {
  sessionId: string;
  title: string;
  operatorId: string;
  createdAt: string;
  updatedAt: string;
};

export type SessionMessageDto = {
  id: string;
  sessionId: string;
  role: string;
  content: string;
  createdAt: string;
};

export type ChatMessage = UIMessage;

export type KnowledgeType = "structured" | "unstructured";

export type KnowledgeIngestRequest = {
  knowledgeType: KnowledgeType;
  sourceName?: string;
  textContent?: string;
  structuredContent?: string;
};

export type KnowledgeReindexResponse = {
  indexed: number;
  provider: string;
};

export type KnowledgeIngestResponse = {
  ingested: number;
  provider: string;
};

export type KnowledgeAskResponse = {
  answer: string;
};
