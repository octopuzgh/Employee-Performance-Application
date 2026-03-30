package com.octopuz.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("performance")
@Builder
public class Performance {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String empNo;
    private Integer year;
    private Integer quarter;
    private BigDecimal score;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
