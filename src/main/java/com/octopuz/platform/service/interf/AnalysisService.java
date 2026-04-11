package com.octopuz.platform.service.interf;

import com.octopuz.platform.vo.*;

import java.math.BigDecimal;
import java.util.List;

public interface AnalysisService{
    // 部门绩效排名
    //List<Map<String, Object>> getDepartmentRank(Integer year, Integer quarter);
    // 员工绩效趋势
    //List<Map<String, Object>> getEmployeeTrend(String empNo);

    DepartmentAvgScoreVO getDepartmentAvgScore(Integer year, Integer quarter, String department);

    CompanyAvgScoreVO getCompanyAvgScore(Integer year, Integer quarter);

    List<DepartmentRankVO> getDepartmentRankVO(Integer year, Integer quarter);

    List<EmployeeTrendVO> getEmployeeTrendVO(String empNo);

    List<DepartmentStatsVO> getDepartmentStats();

    List<EmployeeRankVO> getEmployeeRank(Integer topN);
}
