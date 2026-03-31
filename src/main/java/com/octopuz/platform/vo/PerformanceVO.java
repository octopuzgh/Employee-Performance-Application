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
public class PerformanceVO {
    private Long id;
    private String empNo;
    private Integer year;
    private Integer quarter;
    private BigDecimal score;
}
