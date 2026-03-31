package com.octopuz.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeTrendVO {
    //年份
    private Integer year;
    //季度
    private Integer quarter;
    //分数
    private BigDecimal score;
}
