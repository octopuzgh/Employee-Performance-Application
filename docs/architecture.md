# 绩效数据分析平台 - 架构文档

## 🔄 完整数据链路

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
