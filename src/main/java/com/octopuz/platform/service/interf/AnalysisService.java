package com.octopuz.platform.service.interf;

import com.octopuz.platform.vo.*;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.util.List;

public interface AnalysisService{
    // 部门绩效排名
    //List<Map<String, Object>> getDepartmentRank(Integer year, Integer quarter);
    // 员工绩效趋势
    //List<Map<String, Object>> getEmployeeTrend(String empNo);

    /**
     * 部门排名（按年季度）
     * @param year 年份
     * @param quarter 季度（1-4）
     */
    @Cacheable(value = "analysis:rank", key = "#year + '-' + #quarter")
    List<DepartmentRankVO> getDepartmentRankVO(Integer year, Integer quarter);

    /**
     * 员工绩效趋势
     * @param empNo 员工工号
     */

    @Cacheable(value = "analysis:trend", key = "#empNo")
    List<EmployeeTrendVO> getEmployeeTrendVO(String empNo);
    /**
     * 部门统计（所有部门）
     */
    @Cacheable(value = "analysis:dept-stats")
    List<DepartmentStatsVO> getDepartmentStats();
    /**
     * 员工排名 Top N
     * @param topN 前N名（默认10）
     */
    @Cacheable(value = "analysis:emp-rank")
    List<EmployeeRankVO> getEmployeeRank(Integer topN);
    /**
     * 部门平均分
     * @param year 年份
     * @param quarter 季度（1-4）
     * @param department 部门名称
     */
    @Cacheable(value = "analysis:dept-avg", key = "#year + '-' + #quarter + '-' + #department")
    DepartmentAvgScoreVO getDepartmentAvgScore(Integer year, Integer quarter, String department);

    /**
     * 公司平均分
     * @param year 年份
     * @param quarter 季度（1-4）
     */
    @Cacheable(value = "analysis:company-avg", key = "#year + '-' + #quarter")
    CompanyAvgScoreVO getCompanyAvgScore(Integer year, Integer quarter);

    /**
     * 公司摘要
     * @param year 年份（可选，null表示全部）
     * @param quarter 季度（可选，null表示全部）
     */
    @Cacheable(value = "analysis:company-summary", key = "(#year ?: 'all') + '-' + (#quarter ?: 'all')")
    CompanySummaryVO getCompanySummary(Integer year, Integer quarter);

    /**
     * 异常检测
     * @param threshold 分差阈值（默认20）
     */
    @Cacheable(value = "analysis:anomaly-detect", key = "#threshold ?: '20'")
    List<AnomalyDetectVO> getAnomalyDetect(BigDecimal threshold);}
