package com.octopuz.platform.vo;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyDetectVO {
    @JSONField(name = "emp_no")
    private String empNo;
    private String name;
    private String department;
    private Integer year;
    private Integer quarter;
    @JSONField(name = "current_score")
    private BigDecimal currentScore;
    @JSONField(name = "prev_score")
    private BigDecimal prevScore;
    @JSONField(name = "score_diff")
    private BigDecimal scoreDiff;
}
