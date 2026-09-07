# omes-iomp-ai

AI 编排与工具调用服务，默认端口 `10011`，主类 `com.ourexists.omes.App`。公共环境和本地配置见 [项目总览](../readme.md)。原 `deploy/` 下中英文部署说明合并到本文，环境变量模板继续保留。

## 构建与启动

在仓库根目录执行：

```bash
mvn -pl omes-iomp-ai -am clean package -DskipTests
java -jar omes-iomp-ai/target/omes-iomp-ai-1.0.1-SNAPSHOT.jar
```

运行前准备根目录 `config/config.properties` 和认证配置，配置 PostgreSQL、Redis，以及实际使用的模型服务。默认向量后端为 Qdrant；使用该后端时还需可访问的 Qdrant 实例。AI 通过 `AI_PORTAL_BASE_URL` 调用 Admin 工具接口。

## 配置入口

| 文件 | 用途 |
| --- | --- |
| [application.yml](src/main/resources/application.yml) | 服务、Redis、模型和向量库配置及默认值 |
| [application-db-postgres.yml](src/main/resources/application-db-postgres.yml) | PostgreSQL 数据源 |
| [ai-agent-chain.json](src/main/resources/ai-agent-chain.json) | 内置编排配置 |
| [.env.example](.env.example) | 环境变量示例 |
| [env.prod.example](deploy/env.prod.example) | 英文注释的生产变量模板 |
| [env.prod.zh-CN.example](deploy/env.prod.zh-CN.example) | 中文注释的生产变量模板 |

模板供部署工具注入环境变量，直接运行 JAR 不会自动读取 `.env` 文件。

## 编排与模型

`AI_AGENT_CHAIN_CONFIG_PATH` 默认指向工作目录下的 `config/ai-agent-chain.json`。外部文件不存在时，加载器读取 classpath 内置配置；解析失败时退回代码默认配置并记录警告。

需要自定义时，将 [内置编排文件](src/main/resources/ai-agent-chain.json) 复制到部署目录并修改，再设置 `AI_AGENT_CHAIN_CONFIG_PATH`。旧模板提到的 `config/ai-agent-chain.multi-provider.example.json` 当前不在仓库中，使用模板时应替换成实际文件路径。

节点通过 `provider` 和 `model` 选择模型，按实际使用的供应商设置密钥：

| provider | API Key | 地址配置 |
| --- | --- | --- |
| `openai` | `OPENAI_API_KEY` | `OPENAI_BASE_URL` |
| `deepseek` | `DEEPSEEK_API_KEY` | `DEEPSEEK_BASE_URL` |
| `qwen` | `QWEN_API_KEY` | `QWEN_BASE_URL` |
| `anthropic` | `ANTHROPIC_API_KEY` | `ANTHROPIC_BASE_URL`，需配置兼容端点 |
| `gemini` | `GEMINI_API_KEY` | `GEMINI_BASE_URL`，需配置兼容端点 |

上述 provider 的接入方式以本模块实现为准；Anthropic、Gemini 的地址没有内置默认值，需提供实现所使用的 OpenAI 兼容接口。`AI_DEFAULT_PROVIDER` 默认 `openai`，不能代替节点自己的 provider 配置。

## 部署变量

| 变量 | 用途 / 默认值 |
| --- | --- |
| `REDIS_HOST`、`REDIS_PORT` | 会话与缓存，默认 `127.0.0.1:6379` |
| `AI_PORTAL_BASE_URL` | Admin 地址，默认 `http://127.0.0.1:10010` |
| `AI_AGENT_PORTAL_PROXY_ENABLED` | 门户代理开关，默认 `true` |
| `AGENT_VECTOR_PROVIDER` | 默认 `qdrant` |
| `QDRANT_HOST`、`QDRANT_PORT` | 默认 `localhost:6334` |
| `QDRANT_COLLECTION` | 默认 `omes_inspection_kb` |
| `QDRANT_API_KEY`、`QDRANT_USE_TLS` | 向量库鉴权与 TLS，TLS 默认 `false` |
| `OPENAI_EMBEDDING_MODEL` | 向量检索所用 embedding 模型配置 |
| `AI_AGENT_SESSION_SHORT_TERM_TTL_MINUTES` | 短期会话保留时间，默认 120 分钟 |

部署时核对编排文件是否成功加载、节点模型和密钥是否对应，以及 PostgreSQL、Redis、向量库和 Admin 是否可达。未使用供应商的 Key 可留空。
