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
public class DepartmentRankVO {
    //部门名称
    private String department;
    //平均分数
    private BigDecimal avgScore;
    //员工数量
    private Integer employeeCount;
    //排名
    private Integer departmentRank;
}
