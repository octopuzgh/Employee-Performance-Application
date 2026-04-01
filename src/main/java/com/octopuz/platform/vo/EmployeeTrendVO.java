package com.octopuz.platform.vo;

import com.octopuz.platform.enums.TrendType;
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
    //姓名
    private String name;
    //工号
    private String empNo;
    //年份
    private Integer year;
    //季度
    private Integer quarter;
    //分数
    private BigDecimal score;
    //环比增长百分比（上一季度）
    private BigDecimal growthRate;
    //趋势标识
    private TrendType trend;
}
