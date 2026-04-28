# OMES-AI Deploy Notes

This directory contains production environment templates for `omes-ai`.

## Files

- `env.prod.example`: production-focused environment variable template.
- `README.zh-CN.md`: Chinese deployment guide.

## How provider selection works

`omes-ai` reads model routing from `AI_AGENT_CHAIN_CONFIG_PATH` (a JSON chain file).
Each node can set:

- `provider`: `openai` | `deepseek` | `qwen` | `anthropic` | `gemini`
- `model`: model name for that provider

So the provider keys you must configure depend on which providers are used in your chain.

## Minimum required env vars

Always required:

- `AI_AGENT_CHAIN_CONFIG_PATH`
- `AI_DEFAULT_PROVIDER`
- `REDIS_HOST`, `REDIS_PORT`
- `AI_PORTAL_BASE_URL`
- `OPENAI_EMBEDDING_MODEL` (optional, only for local embedding retrieval)
- Optional Qdrant vector DB:
  - `AGENT_VECTOR_PROVIDER=qdrant`
  - `QDRANT_HOST`, `QDRANT_PORT`, `QDRANT_COLLECTION`
  - `QDRANT_API_KEY` (if cluster auth enabled)
  - `QDRANT_USE_TLS` (`true` for HTTPS)

Provider-specific required keys:

- If any node uses `provider=openai`: set `OPENAI_API_KEY`
- If any node uses `provider=deepseek`: set `DEEPSEEK_API_KEY`
- If any node uses `provider=qwen`: set `QWEN_API_KEY`
- If any node uses `provider=anthropic`: set `ANTHROPIC_API_KEY` and a compatible `ANTHROPIC_BASE_URL`
- If any node uses `provider=gemini`: set `GEMINI_API_KEY` and a compatible `GEMINI_BASE_URL`

## Recommended production setup

- Start from `env.prod.example`
- Use `AI_AGENT_CHAIN_CONFIG_PATH=config/ai-agent-chain.multi-provider.example.json`
- Fill only provider keys actually used by your chain
- Keep unused provider keys empty

## Pre-launch checklist

- `AI_AGENT_CHAIN_CONFIG_PATH` points to an existing file under `config/`
- All providers referenced by chain nodes have valid API keys
- All provider base URLs are OpenAI-compatible endpoints
- Redis is reachable from runtime environment
- If `AGENT_VECTOR_PROVIDER=qdrant`, Qdrant endpoint is reachable
- `AI_PORTAL_BASE_URL` is reachable and auth headers forwarding works
