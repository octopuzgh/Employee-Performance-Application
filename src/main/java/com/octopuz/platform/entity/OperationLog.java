package com.octopuz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("operation_log")
@Builder
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String empNo;
    private String operation;
    private String content;
    private String ipAddress;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
