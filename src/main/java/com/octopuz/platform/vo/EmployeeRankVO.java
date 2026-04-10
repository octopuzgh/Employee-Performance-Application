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
public class EmployeeRankVO {
    private String empNo;
    private String name;
    private String department;
    private String position;
    private BigDecimal avgScore;
    private Integer recordCount;
}
