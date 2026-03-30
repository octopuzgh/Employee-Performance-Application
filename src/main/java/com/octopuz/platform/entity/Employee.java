package com.octopuz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.time.LocalDate;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("employee")
@Builder
public class Employee {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String empNo;
    private String name;
    private String department;
    private String position;
    private LocalDate hireDate;
    private String email;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
