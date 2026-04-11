package com.octopuz.platform.vo;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentStatsVO {
    private String department;
    @JSONField(name = "avg_score")
    private BigDecimal avgScore;
    @JSONField(name = "max_score")
    private BigDecimal maxScore;
    @JSONField(name = "min_score")
    private BigDecimal minScore;
}
