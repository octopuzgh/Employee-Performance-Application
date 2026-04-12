package com.octopuz.platform.controller;

import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.service.interf.AnalysisService;
import com.octopuz.platform.vo.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
@Slf4j
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    @Resource
    private AnalysisService analysisService;
    @GetMapping("/departmentRank")
    public Result<List<DepartmentRankVO>> getDepartmentRank(@RequestParam Integer year, @RequestParam Integer quarter) {
        try {
            List<DepartmentRankVO> result = analysisService.getDepartmentRankVO(year, quarter);
            log.info("查询部门排名成功, year={}, quarter={}", year, quarter);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("查询部门排名失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/employeeTrend")
    public Result<List<EmployeeTrendVO>> getEmployeeTrend(@RequestParam String empNo) {
        try {
            List<EmployeeTrendVO> result = analysisService.getEmployeeTrendVO(empNo);
            log.info("查询员工趋势成功, empNo={}", empNo);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询员工趋势失败, empNo={}", empNo, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/departmentAvgScore")
    public Result<DepartmentAvgScoreVO> getDepartmentAvgScore(
            @RequestParam Integer year,
            @RequestParam Integer quarter,
            @RequestParam String department) {
        try {
            DepartmentAvgScoreVO result = analysisService.getDepartmentAvgScore(year, quarter, department);
            log.info("查询部门平均分成功, year={}, quarter={}, department={}", year, quarter, department);
            return result == null ? Result.error(ResultCode.NOT_FOUND, "没有该部门的绩效数据") : Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("查询部门平均分失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }
    @GetMapping("/companyAvgScore")
    public Result<CompanyAvgScoreVO> getCompanyAvgScore(@RequestParam Integer year, @RequestParam Integer quarter) {
        try {
            CompanyAvgScoreVO result = analysisService.getCompanyAvgScore(year, quarter);
            log.info("查询公司平均分成功, year={}, quarter={}", year, quarter);
            return result == null ? Result.error(ResultCode.NOT_FOUND, "没有该公司的绩效数据") : Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("查询公司平均分失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }
    @GetMapping("/departmentStats")
    public Result<List<DepartmentStatsVO>> getDepartmentStats() {
        try {
            List<DepartmentStatsVO> result = analysisService.getDepartmentStats();
            log.info("查询部门统计成功");
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询部门统计失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/employeeRank")
    public Result<List<EmployeeRankVO>> getEmployeeRank(@RequestParam(defaultValue = "10") Integer topN) {
        try {
            List<EmployeeRankVO> result = analysisService.getEmployeeRank(topN);
            log.info("查询员工排名成功, topN={}", topN);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询员工排名失败, topN={}", topN, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }
    @GetMapping("/companySummary")
    public Result<CompanySummaryVO> getCompanySummary(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter) {
        try {
            CompanySummaryVO result = analysisService.getCompanySummary(year, quarter);
            log.info("查询公司摘要成功, year={}, quarter={}", year, quarter);
            return result == null ? Result.error(ResultCode.NOT_FOUND, "没有公司摘要数据") : Result.success(result);
        } catch (Exception e) {
            log.error("查询公司摘要失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/anomalyDetect")
    public Result<List<AnomalyDetectVO>> getAnomalyDetect(
            @RequestParam(required = false) BigDecimal threshold) {
        try {
            List<AnomalyDetectVO> result = analysisService.getAnomalyDetect(threshold);
            log.info("查询异常检测成功, threshold={}", threshold);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询异常检测失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }


}
