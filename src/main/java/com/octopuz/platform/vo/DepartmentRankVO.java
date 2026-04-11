package com.octopuz.platform.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DepartmentRankVO {
    private String department;
    @JSONField(name = "avg_score")
    private BigDecimal avgScore;
    @JSONField(name = "max_score")
    private BigDecimal maxScore;
    @JSONField(name = "min_score")
    private BigDecimal minScore;
    @JSONField(name = "emp_count")
    private Integer employeeCount;
    @JSONField(name = "rank_num")
    private Integer departmentRank;
}
