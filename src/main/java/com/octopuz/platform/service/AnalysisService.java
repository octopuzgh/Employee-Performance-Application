package com.octopuz.platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopuz.platform.entity.Performance;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AnalysisService{
    // 部门绩效排名
    List<Map<String, Object>> getDepartmentRanK(Integer year, Integer quarter);
    // 员工绩效趋势
    List<Map<String, Object>> getEmployeeTrend(String empNo);
    //部门平均分
    BigDecimal getDepartmentAvgScore(Integer year, Integer quarter,String department);
    //公司平均分
    BigDecimal getCompanyAvgScore(Integer year, Integer quarter);
}
