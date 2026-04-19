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

    @Override
    public String toString() {
        return this.message;
    }

    //根据分数变化判断趋势
    public static TrendType fromGrowthRate(BigDecimal growthRate) {
        if (growthRate == null) return FIRST;
        if (growthRate.compareTo(BigDecimal.ZERO) > 0) return UP;
        if (growthRate.compareTo(BigDecimal.ZERO) < 0) return DOWN;
        return STABLE;
    }

}
