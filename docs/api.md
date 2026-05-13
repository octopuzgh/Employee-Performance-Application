# 绩效数据分析平台 - API 文档

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

## 接口详情（示例）

### 1. 部门排名

- **URL**: `/api/analysis/departmentRank`
- **方法**: `GET`
- **参数**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | year | Integer | 是 | 年份 |
  | quarter | Integer | 是 | 季度（1-4） |
- **返回示例**:
```json
[
  {
    "department": "技术部",
    "avgScore": 95.5,
    "rankNum": 1
  }
]
```

### 2. 员工趋势

- **URL**: `/api/analysis/employeeTrend`
- **方法**: `GET`
- **参数**:
  | 参数 | 类型 | 必填 | 说明 |
  |------|------|------|------|
  | empNo | String | 是 | 工号 |
- **返回示例**:
```json
[
  {
    "year": 2024,
    "quarter": 1,
    "score": 85.5,
    "growthRate": null
  },
  {
    "year": 2024,
    "quarter": 2,
    "score": 90.0,
    "growthRate": 5.26
  }
]
```
