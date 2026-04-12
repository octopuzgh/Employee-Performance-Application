
# 踩坑记录
###### 主要记得的坑
## 1. MapStruct Bean 找不到

**现象**：启动报错 `A component required a bean of type 'EmployeeConverter' that could not be found.`

**原因**：MapStruct 是编译期生成实现类，Lombok 和 MapStruct 注解处理器冲突，或修改后未重新编译。

**解决**：
1. 将 Lombok 和 MapStruct 放在同一个 `annotationProcessorPaths` 中
2. 执行 `mvn clean compile` 重新编译

---

## 2. PySpark 冷启动慢

**现象**：统计接口首次调用 10-11 秒，缓存命中后 20-50 毫秒。删缓存后再调用 9-10 秒。

**原因**：Spark Session 初始化耗时（JVM 启动 + Context 创建 + JDBC 加载），SSH + Python 解释器启动也有开销。

**解决**：
1. SSH 超时设为 60 秒
2. 前端提示"首次查询可能较慢"
3. 依赖 Redis 缓存补偿

---

## 3. Redis 缓存清除遗漏

**现象**：更新绩效数据后，部分统计接口仍返回旧数据。

**原因**：`@CacheEvict` 只清除了 3 个缓存，遗漏了 5 个。

**解决**：补全所有缓存 namespace：

```java
@CacheEvict(value = {
    "analysis:rank",
    "analysis:dept-avg",
    "analysis:company-avg",
    "analysis:trend",
    "analysis:dept-stats",
    "analysis:emp-rank",
    "analysis:company-summary",
    "analysis:anomaly-detect"
}, allEntries = true)
```

---

## 4. Redisson 分布式锁未释放

**现象**：Excel 导入失败后，锁一直存在 Redis，后续所有导入都被拒绝。

**原因**：finally 块中 lock.isHeldByCurrentThread() 在某些异常路径返回 false，或 unlock() 异常被吞掉。

**解决**：

```java
finally {
    if (lock != null && lock.isHeldByCurrentThread()) {
        try {
            lock.unlock();
        } catch (Exception e) {
            log.error("释放锁失败", e);
        }
    }
}
```

同时设置 leaseTime，防止永久锁定：

```java
lock.tryLock(0, 300, TimeUnit.SECONDS);
```

---

## 5. Fastjson2 解析 Decimal 类型

**现象**：PySpark 返回的 JSON 中，分数字段解析后为 null 或精度丢失。

**原因**：Python 的 Decimal 序列化为 JSON 时是数字类型，Fastjson2 默认行为与预期不符。

**解决**：Python 端将 Decimal 转为字符串。

```python
class CustomJSONEncoder(json.JSONEncoder):
    """自定义 JSON 编码器，处理 Decimal、date、datetime 类型"""

    def default(self, obj):
        if isinstance(obj, Decimal):
            return float(obj)
        elif isinstance(obj, (date, datetime)):
            return obj.strftime("%Y-%m-%d %H:%M:%S")
        return super().default(obj)
```
---

## 6. 普通 CRUD 首次查询慢

**现象**：查单个员工，第一次约 100 毫秒，后续降至 8 毫秒。

**原因**：MyBatis-Plus 首次加载映射、MySQL 连接池初始化、热点索引第一次加载。

**解决**：正常现象，无需处理。连接池和索引预热后自动优化。
