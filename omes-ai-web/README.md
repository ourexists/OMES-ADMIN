# OMES AI Web (Next.js)

## 启动

1. 复制环境变量

```bash
cp .env.example .env.local
```

2. 安装依赖并启动

```bash
npm install
npm run dev
```

默认读取 `NEXT_PUBLIC_OMES_AI_BASE_URL`，后端需运行在 `omes-ai` 并开放接口：

- `POST /inspection/ai/agent/multi-chat/stream` (SSE)
- `GET /inspection/ai/agent/multi-chat/config`
- `GET /inspection/ai/agent/multi-chat/session/list`
- `POST /inspection/ai/agent/multi-chat/session/create`
- `GET /inspection/ai/agent/multi-chat/session/messages`
