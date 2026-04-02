package com.octopuz.platform.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeExcel {
    @ExcelProperty("员工编号")
    private String empNo;
    @ExcelProperty("员工姓名")
    private String name;
    @ExcelProperty("部门")

    private String department;
    @ExcelProperty("职位")
    private String position;
    @ExcelProperty("入职日期")
    @DateTimeFormat("yyyy-MM-dd")
    private LocalDate hireDate;
    @ExcelProperty("邮箱")
    private String email;
}
