# 绩效数据分析平台（Java + PySpark 混合架构）

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.13-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/PySpark-4.1.1-orange" alt="PySpark">
  <img src="https://img.shields.io/badge/Kafka-3.x-black" alt="Kafka">
  <img src="https://img.shields.io/badge/Redis-6.x-red" alt="Redis">
  <img src="https://img.shields.io/badge/MySQL-8.0-blue" alt="MySQL">
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License">
</p>

## 📌 项目简介

**Java + PySpark 混合架构**实践项目，覆盖 **数据接入 → 数据计算 → 数据服务** 完整链路。

- **数据接入**：Excel 批量导入、Kafka 异步日志
- **数据计算**：PySpark + Spark SQL（窗口函数 RANK/LAG）
- **数据服务**：Spring Boot REST API，Redis 缓存加速

业务层与计算层通过 **SSH + JSON** 完全解耦。

---

## 🛠 技术栈

| 类别 | 技术 | 用途 |
| :--- | :--- | :--- |
| 混合架构 | Spring Boot + PySpark | 业务与计算解耦 |
| 计算引擎 | Spark SQL、窗口函数 | 8 个统计接口 |
| 缓存/锁 | Redis、Redisson | 缓存统计结果 + 导入防并发 |
| 消息队列 | Kafka | 操作日志异步解耦 |
| 通信协议 | SSH + JSON | 跨语言调用 |
| 数据库 | MySQL | 数据存储 |

---

## 🎯 核心亮点

- **混合架构**：Java + PySpark，SSH + JSON 跨语言通信
- **性能优化**：Redis 缓存，首次调用 10‑12 秒 → 缓存命中 10‑20 毫秒（**提升 500‑1200 倍**）
- **完整链路**：数据接入 → 计算 → 服务
- **工程实践**：Redisson 分布式锁、Kafka 异步日志、EasyExcel 流式解析

---

## 📊 核心指标

- **8 个统计接口**，支持部门排名、员工趋势、异常检测
- **响应时间**：首次 10‑12 秒 / 缓存命中 10‑20 毫秒
- **Excel 导入**：1000 条员工 ~2.8 秒，4000 条绩效 ~8.1 秒

---

## 🔗 详细文档

- [完整架构与数据链路](./docs/architecture.md)
- [8 个接口 API 文档](./docs/api.md)

---

## 🚀 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+
- Python 3.10+（PySpark 4.1.1）
- Linux 虚拟机（用于运行 PySpark）

### 方式一：传统部署

所有服务（MySQL、Redis、Kafka、Spark）部署在 Linux 虚拟机中，Spring Boot 在 Windows/Linux 运行。

```bash
# 1. 克隆项目
git clone https://github.com/octopuzgh/Employee-Performance-Application.git
cd Employee-Performance-Application

# 2. 配置 application.yml（数据库、Redis、Kafka、SSH）

# 3. 启动 Spring Boot
mvn spring-boot:run

# 4. 在 Linux 虚拟机中运行 PySpark 脚本
cd scripts && python3 main.py dept_rank 2024 1
```
方式二：Docker 快速启动（可选）
项目提供了 docker-compose.yml，可一键启动 MySQL、Redis、Kafka 等依赖服务，适合快速体验。

```bash
# 启动所有依赖服务
docker-compose up -d

# 查看服务状态
docker-compose ps
注意：Spark 计算引擎仍需在 Linux 虚拟机中运行，Docker 仅用于快速启动辅助服务。
```
📄 许可证
MIT
## 🙋 作者

**octopuz** · [GitHub](https://github.com/octopuzgh)

---

<p align="center">
  <sub>Built with ☕ by octopuz</sub>
</p>