package com.octopuz.platform.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerformanceExcel {
    @ExcelProperty("员工编号")
    private String empNo;
    @ExcelProperty("年份")
    private Integer year;
    @ExcelProperty("季度")
    private Integer quarter;
    @ExcelProperty("绩效分数")
    private BigDecimal score;
}
