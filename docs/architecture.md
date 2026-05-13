# 绩效数据分析平台 - 架构文档

## 📁 项目结构

<details>
<summary>📁 点击展开/收起项目结构</summary>

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

## 🔄 完整数据链路

### 链路一：数据接入（Excel 批量导入 + Kafka 日志）
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

text

**涉及技术**：EasyExcel + Redisson + MyBatis-Plus + Kafka

---

### 链路二：数据计算（Spark SQL 统计分析）
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

text

**涉及技术**：JSch + SSH + PySpark + Spark SQL + Redis

---

### 链路三：数据服务（REST API + MapStruct 转换）
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

text

**涉及技术**：Spring Boot + MapStruct + Fastjson2

---

### 链路四：基础业务（员工/绩效 CRUD）
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

text

**涉及技术**：Spring Boot + MyBatis-Plus + MapStruct + Kafka

> 虽然是数据平台项目，但完整保留了后端 CRUD 能力，体现全栈开发功底。