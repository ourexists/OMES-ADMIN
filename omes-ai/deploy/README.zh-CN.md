# OMES-AI 部署说明（中文）

本目录用于存放 `omes-ai` 的生产部署环境模板。

## 文件说明

- `env.prod.example`：英文版生产环境变量模板
- `env.prod.zh-CN.example`：中文版生产环境变量模板（推荐运维直接使用）

## Provider 与编排链路的关系

系统通过 `AI_AGENT_CHAIN_CONFIG_PATH` 指向的 JSON 编排文件决定每个节点调用哪个模型。
每个节点可配置：

- `provider`：`openai` | `deepseek` | `qwen` | `anthropic` | `gemini`
- `model`：对应 provider 的模型名

结论：**链路里用了哪个 provider，就必须配置哪个 provider 的 API Key。**

## 最小必填变量

基础必填：

- `AI_AGENT_CHAIN_CONFIG_PATH`
- `AI_DEFAULT_PROVIDER`
- `REDIS_HOST`, `REDIS_PORT`
- `AI_PORTAL_BASE_URL`
- `OPENAI_EMBEDDING_MODEL`（可选，仅本地 embedding 检索时生效）
- 可选 Qdrant 向量库：
  - `AGENT_VECTOR_PROVIDER=qdrant`
  - `QDRANT_HOST`, `QDRANT_PORT`, `QDRANT_COLLECTION`
  - `QDRANT_API_KEY`（集群开启鉴权时填写）
  - `QDRANT_USE_TLS`（HTTPS 时设为 `true`）

按 provider 必填：

- 任一节点 `provider=openai`：必须配置 `OPENAI_API_KEY`
- 任一节点 `provider=deepseek`：必须配置 `DEEPSEEK_API_KEY`
- 任一节点 `provider=qwen`：必须配置 `QWEN_API_KEY`
- 任一节点 `provider=anthropic`：必须配置 `ANTHROPIC_API_KEY` 和兼容 `ANTHROPIC_BASE_URL`
- 任一节点 `provider=gemini`：必须配置 `GEMINI_API_KEY` 和兼容 `GEMINI_BASE_URL`

## 推荐生产配置

- 从 `env.prod.zh-CN.example` 复制
- 使用：
  - `AI_AGENT_CHAIN_CONFIG_PATH=config/ai-agent-chain.multi-provider.example.json`
- 仅填写链路实际使用 provider 的 Key
- 未使用 provider 的 Key 保持为空

## 上线前检查清单

- `AI_AGENT_CHAIN_CONFIG_PATH` 指向的文件在 `config/` 下真实存在
- 编排节点涉及的 provider 均已配置有效 API Key
- 所有 provider 的 `BASE_URL` 为可用的 OpenAI 兼容地址
- Redis 从部署环境可连通
- 若 `AGENT_VECTOR_PROVIDER=qdrant`，Qdrant 地址可连通
- `AI_PORTAL_BASE_URL` 可达，且请求头透传正常
