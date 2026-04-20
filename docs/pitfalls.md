
# 踩坑记录

> 记录开发过程中遇到的实际问题及解决方案。

---

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

**原因**：finally 块中 `lock.isHeldByCurrentThread()` 在某些异常路径返回 false，或 `unlock()` 异常被吞掉。

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

**原因**：Python 的 `Decimal` 序列化为 JSON 时是数字类型，Fastjson2 默认行为与预期不符。

**解决**：Python 端编写自定义 JSON 编码器，将 Decimal 转为 float：

```python
class CustomJSONEncoder(json.JSONEncoder):
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

---

## 7. MyBatis-Plus 分页插件找不到

**现象**：启动报错 `No qualifying bean of type 'PaginationInterceptor' available`

**原因**：MyBatis-Plus 3.1.0 以上版本中，分页插件包名和类名发生了变更。

**解决**：

### 新版本（3.1.0 及以上）
```java
@Bean
public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
    return interceptor;
}
```

同时引入 `jsqlparser` 依赖：

```xml
<dependency>
    <groupId>com.github.jsqlparser</groupId>
    <artifactId>jsqlparser</artifactId>
    <version>4.6</version>
</dependency>
```

---

## 8. Lombok 注解不生效

**现象**：`@Data`、`@Builder`、`@AllArgsConstructor` 等 Lombok 注解不生效，IDE 报红或编译失败。

**原因**：
1. IDEA 中 Lombok 插件未安装或未启用
2. 注解处理器未开启
3. `pom.xml` 中未显式指定 Lombok 版本

**解决**：

### 临时解决
```bash
mvn clean compile
```

### 永久解决

1. **安装 Lombok 插件**：IDEA → Settings → Plugins → 搜索 Lombok → 安装并重启

2. **开启注解处理器**：IDEA → Settings → Compiler → Annotation Processors → 勾选 "Enable annotation processing"

3. **在 `pom.xml` 中显式指定版本**：

```xml
<properties>
    <lombok.version>1.18.30</lombok.version>
</properties>

<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>${lombok.version}</version>
    <scope>provided</scope>
</dependency>
```

4. **与 MapStruct 共存时**，两者放在同一个 `annotationProcessorPaths` 中：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </path>
            <path>
                <groupId>org.mapstruct</groupId>
                <artifactId>mapstruct-processor</artifactId>
                <version>${mapstruct.version}</version>
            </path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

## 9. MyBatis-Plus 乐观锁与自动填充冲突

**现象**：使用乐观锁（`@Version`）时，更新操作无法自动刷新 `update_time` 字段。

```java
User user = userMapper.selectById(1L);  // updateTime = 昨天
user.setName("新名字");
userMapper.updateById(user);
// ❌ updateTime 还是昨天，没有变成当前时间
```

**原因**：

| 机制 | 要求 | 导致的问题 |
|------|------|-----------|
| 乐观锁 | 必须先查，拿到旧值 | 查出来的 `updateTime` 有值（非 null） |
| 自动填充 | `strictUpdateFill` 只在字段为 `null` 时填充 | 因为有值，所以不填充 |

两种机制的设计冲突。

**解决**：

### 方案一：使用 `setFieldValByName` 强制填充（推荐）

```java
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    
    @Override
    public void updateFill(MetaObject metaObject) {
        // 强制填充，不管字段原来有没有值
        this.setFieldValByName("updateTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("updatedAt", LocalDateTime.now(), metaObject);
    }
}
```

### 方案二：使用 `@TableField(update = "NOW()")`

```java
@TableField(update = "NOW()")
private LocalDateTime updateTime;
```

不依赖 `MetaObjectHandler`，直接在 SQL 层面更新时间。

### 方案三：更新前手动设为 `null`

```java
user.setUpdateTime(null);
userMapper.updateById(user);
```

### 方案对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| `setFieldValByName` | 一次配置，全局生效 | 需要理解 MP 源码 |
| `update = "NOW()"` | 最简单，不依赖 Handler | 依赖数据库函数 |
| 手动设 `null` | 直观 | 每个更新都要写，容易漏 |

**教训**：MyBatis-Plus 的自动填充和乐观锁在设计上有冲突，不能直接组合使用。简单时间戳用 `update = "NOW()"`，复杂填充逻辑用 `setFieldValByName` 强制覆盖。

