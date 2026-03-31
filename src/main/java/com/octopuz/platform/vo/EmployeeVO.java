package com.octopuz.platform.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmployeeVO {
    private Long id;
    private String empNo;
    private String name;
    private String department;
    private String position;
    private LocalDate hireDate;
    private String email;
}
