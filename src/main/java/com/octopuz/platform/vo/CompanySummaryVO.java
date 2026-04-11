package com.octopuz.platform.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanySummaryVO {
    @JSONField(name = "total_employees")
    private Integer totalEmployees;

    @JSONField(name = "avg_score")
    private BigDecimal avgScore;

    @JSONField(name = "max_score")
    private BigDecimal maxScore;

    @JSONField(name = "min_score")
    private BigDecimal minScore;

    @JSONField(name = "total_records")
    private Integer totalRecords;

    @JSONField(name = "dept_count")
    private Integer deptCount;

    @JSONField(name = "max_score_employee")
    private EmployeeScoreDetailVO maxScoreEmployee;

    @JSONField(name = "min_score_employee")
    private EmployeeScoreDetailVO minScoreEmployee;
}
