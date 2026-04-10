package com.octopuz.platform.vo;

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
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
}
