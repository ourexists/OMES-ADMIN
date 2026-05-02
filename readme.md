OMES 工业设备管理平台
===============

[![AUR](https://img.shields.io/badge/license-AGPL%203.0-blue.svg)]()
[![](https://img.shields.io/badge/Author-ourexists-orange.svg)]()
[![](https://img.shields.io/badge/version-1.0.0-brightgreen.svg)]()

介绍
-----------------------------------
本平台聚焦工业设备管理领域，构建涵盖设备全生命周期管理、统一数据采集与治理、实时分析与建模的技术体系；基于智能巡检与设备健康评估模型，实现预测性维护与故障预警；并通过场景化建模与业务编排能力，支撑复杂工业场景下的数字化运营与决策优化


> 项目介绍：https://blog.ourexists.site/2026/01/30/omes/


平台生态:
-----------------------------------
 * OMES-admin管理平台
 * OMES智设备（移动端）
 * OMES-VIDEO (边缘图像检测)。 


交流与支持
-----------------------------------

- 微信： m15026681077
- 邮件： 434713950@163.com

版本对应表

| OMES Version | 变更内容               | Era Framework Version | Layui Version | Spring Boot Version | JAVA Version |
|:-------------|:-------------------|:---------------------|:--------------|:--------------------|:-------------|           
| 1.0.0-SNAPSHOT     | | 2024.0.1             | 2.9.27      | 3.4.5               | 21           |

![img.png](架构图.png)

启动说明（Flink + RabbitMQ）
-----------------------------------

`omes-portal` 内嵌 Flink `RMQSource` 在 JDK 17/21 下运行时，需要开放部分 JDK 模块给 Flink/Chill 反射访问。

1. IDEA 启动（推荐在运行配置中设置）  
在 `OMES-ADMIN` 对应的 Run Configuration 的 `VM options` 增加：

```text
--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED
```

2. `java -jar` 启动  
可以在启动命令中追加相同参数，或通过环境变量统一注入：

```text
JAVA_TOOL_OPTIONS=--add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.lang=ALL-UNNAMED
```

3. 现象说明  
若未配置上述参数，RabbitMQ 消费线程可能出现如下异常并反复重启：  
`InaccessibleObjectException: module java.base does not "opens java.util" ...`

---
进行中: 生产计划流程结合

> 项目依赖个人私有的框架包，无法下载直接运行。仅供开源功能参考，如需定制可联系我