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
public class EmployeeRankVO {
    @JSONField(name = "emp_no")
    private String empNo;
    private String name;
    private String department;
    private String position;
    @JSONField(name = "avg_score")
    private BigDecimal avgScore;
    @JSONField(name = "record_count")
    private Integer recordCount;
}
