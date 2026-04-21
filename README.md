
# 绩效数据分析平台（轻量级数据平台方向）

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.5.13-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/MyBatis--Plus-3.5.15-blue" alt="MyBatis-Plus">
  <img src="https://img.shields.io/badge/PySpark-4.1.1-orange" alt="PySpark">
  <img src="https://img.shields.io/badge/Kafka-3.x-black" alt="Kafka">
  <img src="https://img.shields.io/badge/Redis-6.x-red" alt="Redis">
  <img src="https://img.shields.io/badge/MySQL-8.0-pink" alt="MySQL">
  <img src="https://img.shields.io/badge/License-MIT-green" alt="License">
</p>

## 📌 项目简介

**绩效数据分析平台** 是一个轻量级数据平台实践项目，覆盖 **数据接入 → 数据计算 → 数据服务** 的完整链路。

- **数据接入**：Excel 批量导入、Kafka 异步日志
- **数据计算**：PySpark 执行 Spark SQL 统计分析（窗口函数 RANK/LAG）
- **数据服务**：Spring Boot 提供 REST API，Redis 缓存加速

项目采用 **Java + Python 混合架构**，业务层与计算层通过 SSH + JSON 解耦。

---

## 🛠 技术框架

| 类别 | 技术 | 版本          | 用途 |
| :--- | :--- |:------------| :--- |
| 核心框架 | Spring Boot | 3.5.13      | 应用基础框架 |
| ORM 增强 | MyBatis-Plus | 3.5.15      | 简化 CRUD 操作 |
| 对象转换 | MapStruct | 1.5.5.Final | VO ↔ Entity 编译期转换 |
| 缓存 | Redis + Spring Cache | 6.x         | 统计接口结果缓存 |
| 分布式锁 | Redisson | 3.24.3      | Excel 导入防并发 |
| 消息队列 | Kafka | 3.x         | 操作日志异步解耦 |
| 大数据计算 | PySpark | 4.1.1       | 8 个 Spark SQL 统计接口 |
| 跨语言通信 | JSch + SSH | 0.2.17      | Java 调用远程 Python |
| JSON 解析 | Fastjson2 | 2.0.40      | 解析 PySpark 返回结果 |
| Excel 处理 | EasyExcel | 3.3.2       | 批量导入导出 |
| 数据库 | MySQL | 8.0         | 数据存储 |
| 容器化（可选） | Docker / Docker Compose | 3.8         | 快速启动依赖服务 |

---

## 📁 项目结构

<details>
<summary>点击展开完整目录树</summary>

```
platform/
├── src/main/java/com/octopuz/platform/
│ ├── controller/ # REST API 控制器
│ │ ├── AnalysisController.java # 8 个统计接口
│ │ ├── EmployeeController.java # 员工 CRUD + 导入导出
│ │ └── PerformanceController.java # 绩效 CRUD + 导入导出
│ │
│ ├── service/ # 业务逻辑层
│ │ ├── interf/ # 接口定义
│ │ └── impl/ # 实现类
│ │
│ ├── mapper/ # 数据库映射,mp提供crud
│ ├── converter/ # MapStruct 转换器
│ │ ├── EmployeeConverter.java
│ │ └── PerformanceConverter.java
│ │
│ ├── utils/ # 工具类
│ │ ├── PythonScriptExecutor.java # 构建 SSH 命令
│ │ ├── SshExecutor.java # SSH 底层执行
│ │ ├── KafkaSender.java # Kafka 消息发送(生产者)
│ │ └── UserContextUtil.java # 获得用户ip和信息(后续配合登录信息用)
│ │
│ ├── config/ # 配置类
│ ├── consumer/ # Kafka 消费者
│ ├── handler/ # MyBatis-Plus 自动填充
│ ├── listener/ # EasyExcel 监听器
│ ├── entity/ # 数据库实体
│ ├── vo/ # 视图对象│ ├── dto/ # excel数据传入对象
│ ├── enums/ # 枚举类
│ │ └── TrendType.java # 目前没什么用但是用了的枚举类，用于判断趋势
│ ├── common/ # 通用响应封装
│ └── PlatformApplication.java # 启动类
│   
├── scripts/ # PySpark 脚本目录
│ ├── main.py # 统一入口（路由分发）
│ ├── common/ # 公共模块
│ │ ├── db_utils.py # MySQL 读取
│ │ └── spark_utils.py # Spark Session 创建
│ ├── config/ # 配置模块
│ │ └── settings.py # 数据库/Spark 配置
│ ├── statistics/ # 8 个统计脚本
│ │ ├── dept_rank.py # 部门排名（RANK 窗口函数）
│ │ ├── emp_rank.py # 员工排名
│ │ ├── emp_trend.py # 员工趋势（LAG 窗口函数）
│ │ ├── dept_stats.py # 部门统计
│ │ ├── dept_avg.py # 部门平均绩效
│ │ ├── company_summary.py # 公司摘要
│ │ ├── company_avg.py # 公司平均绩效
│ │ └── anomaly_detect.py # 异常检测（LAG 窗口函数）
│ └── logs/ # 运行日志
│
├── docker-compose.yml # Docker 快速启动配置（可选）
└── pom.xml
```

</details>

---

## 🔄 数据平台链路

### 链路一：数据接入（Excel 批量导入 + Kafka 日志）

```
Excel 上传
↓
Redisson 分布式锁（防并发）
↓
EasyExcel 流式解析（避免 OOM）
↓
数据校验 + 批量保存（MyBatis-Plus saveBatch）
↓
Kafka 异步发送操作日志（解耦）
↓
返回导入结果
```

**涉及技术**：EasyExcel + Redisson + MyBatis-Plus + Kafka

---

### 链路二：数据计算（Spark SQL 统计分析）

```
前端请求统计接口
↓
Redis 缓存命中 → 直接返回（~10-20ms）
↓（未命中）
SSH 调用 Linux 虚拟机
↓
PySpark 读取 MySQL → Spark SQL 统计
├── 窗口函数（RANK / LAG）
└── Catalyst 优化器自动优化
↓
JSON 返回 → 存入 Redis
```

**涉及技术**：JSch + SSH + PySpark + Spark SQL + Redis

---

### 链路三：数据服务（REST API + MapStruct 转换）

```
统计结果 JSON / 数据库 Entity
↓
MapStruct 编译期转换（VO ↔ Entity）
├── 比 BeanUtils 快 10 倍以上
└── 类型安全，避免反射
↓
Fastjson2 解析 JSON
↓
Spring Boot REST API
↓
统一响应封装（Result + ResultCode）
↓
返回前端
```

**涉及技术**：Spring Boot + MapStruct + Fastjson2

---

### 链路四：基础业务（员工/绩效 CRUD）

```
前端请求（增删改查）
↓
Controller 参数校验
↓
Service 业务逻辑
↓
MapStruct VO ↔ Entity 转换
↓
MyBatis-Plus Mapper（自动填充、逻辑删除）
↓
MySQL
↓
Kafka 异步记录操作日志
↓
返回前端
```

**涉及技术**：Spring Boot + MyBatis-Plus + MapStruct + Kafka

> 虽然是数据平台项目，但完整保留了后端 CRUD 能力，体现全栈开发功底。

---

## 📊 统计接口清单（8个）

| 接口 | 功能 | 窗口函数 | 缓存 |
| :--- | :--- | :--- | :---: |
| `/departmentRank` | 部门排名（同分同名次） | `RANK()` | ✅ |
| `/employeeTrend` | 员工趋势（环比增长率） | `LAG()` | ✅ |
| `/departmentStats` | 部门综合统计 | - | ✅ |
| `/employeeRank` | 员工 Top N | - | ✅ |
| `/departmentAvgScore` | 部门平均分 | - | ✅ |
| `/companyAvgScore` | 公司平均分 | - | ✅ |
| `/companySummary` | 公司摘要 | - | ✅ |
| `/anomalyDetect` | 异常检测（分差 > 阈值） | `LAG()` | ✅ |

---

## 📊 性能数据

> 测试环境：员工表 1000+ 条，绩效表 4000+ 条

| 场景 | 耗时        | 说明                               |
| :--- |:----------|:---------------------------------|
| **统计接口（首次调用）** | ~10-12 秒  | PySpark 冷启动 + Spark Session 初始化  |
| **统计接口（缓存命中）** | ~10-20 毫秒 | Redis 直接返回，提升 **500-1200 倍**     |
| **统计接口（缓存失效后）** | ~7-9 秒    | 需重新初始化 Spark（但比首次略快）             |
| **Excel 导入（1000 员工）** | ~2.8 秒    | EasyExcel 流式 + MyBatis-Plus 批量插入 |
| **Excel 导入（4000 绩效）** | ~8.1 秒    | 同上                               |
| **普通 CRUD（首次）** | ~100 毫秒   | MyBatis-Plus + MySQL 首次查询        |
| **普通 CRUD（后续）** | ~8 毫秒     | 数据库连接池 + 热点索引生效                  |

### 📌 关键发现

- **PySpark 冷启动是主要瓶颈**（10-11 秒），但缓存命中后性能极佳（20-50 毫秒）
- **普通 CRUD 首次慢（100ms）**，后续降至 8ms——连接池 + 索引生效

---

## 🎯 核心设计决策

| 决策 | 理由 |
| :--- | :--- |
| **Java + PySpark 混合架构** | 业务层用 Java，计算层用 Spark，解耦且可独立扩展 |
| **SSH + JSON 通信** | 简单直接，无需额外服务，适合跨语言调用 |
| **MapStruct 对象转换** | 编译期生成代码，比 BeanUtils 快 10 倍以上 |
| **Redisson 分布式锁** | Excel 导入时防并发，tryLock + finally 双重保护 |
| **Kafka 异步日志** | 操作日志异步写入，手动 ack 保证不丢消息 |
| **Redis 缓存** | 统计接口命中后响应时间从 10 秒降到 20-50 毫秒 |
| **窗口函数优先** | `RANK()` 处理同分排名，`LAG()` 计算环比，避免自连接 |

---

## 🚀 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Python 3.10+（PySpark 4.1.1）
- Linux 虚拟机（用于运行 PySpark 和依赖服务）

### 方式一：传统部署（推荐）

所有服务（MySQL、Redis、Kafka、Spark）部署在 Linux 虚拟机中，Spring Boot 在 Windows/Linux 运行。

```bash
# 1. 克隆项目
git clone https://github.com/octopuzgh/Employee-Performance-Application.git
cd Employee-Performance-Application

# 2. 配置 application.yml（数据库、Redis、Kafka、SSH）

# 3. 启动 Spring Boot
mvn spring-boot:run

# 4. 在 Linux 虚拟机中运行 PySpark 脚本
cd /path/to/scripts
python3 main.py dept_rank 2024 1
```

### 方式二：Docker 快速启动（可选）

项目提供了 `docker-compose.yml`，可一键启动 MySQL、Redis、Kafka 等依赖服务，适合快速体验。

```bash
# 启动所有依赖服务
docker-compose up -d

# 查看服务状态
docker-compose ps
```

> **注意**：Spark 计算引擎仍需在 Linux 虚拟机中运行，Docker 仅用于快速启动辅助服务。

---

## 📌 后续优化计划

- [ ] 全局异常处理（`@RestControllerAdvice`）
- [ ] 缓存 TTL 配置 + 精细清除
- [ ] PySpark 预热缓存（避免首次冷启动）
- [ ] 集成 Swagger 接口文档
- [ ] 密码加密（Jasypt 或环境变量）
- [ ] 为SSH加连接池或使用http服务
---

## 📄 许可证

[MIT](LICENSE)

---

## 🙋 作者

**octopuz** · [GitHub](https://github.com/octopuzgh)

---

<p align="center">
  <sub>Built with ☕ by octopuz</sub>
</p>