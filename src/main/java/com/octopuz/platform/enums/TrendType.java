package com.octopuz.platform.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor


public enum TrendType {
    UP("UP", "上升"),
    DOWN("DOWN", "下降"),
    STABLE("STABLE", "保持不变"),
    FIRST("FIRST", "首期");
    private final String code;
    private final String message;

    //获得枚举
    public static TrendType getByCode(String code) {
        for (TrendType value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }

    //根据分数变化判断趋势
    public static TrendType getTrendType(BigDecimal currentScore, BigDecimal prevScore) {
        if (prevScore == null) return FIRST;
        if (currentScore.compareTo(prevScore) > 0) {
            return UP;
        } else if (currentScore.compareTo(prevScore) < 0) {
            return DOWN;
        } else {
            return STABLE;
        }
    }

}
