package com.octopuz.platform.controller;

import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.service.interf.AnalysisService;
import com.octopuz.platform.vo.DepartmentRankVO;
import com.octopuz.platform.vo.EmployeeTrendVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

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
        log.info("开始查询部门排名... year = {},quarter={}", year, quarter);
        //季度必须在1-4之间
        if (quarter < 1 || quarter > 4) {
            return Result.error(ResultCode.BAD_REQUEST, "季度必须在1-4之间");
        }
        //处理查询失败或查询结果为空的情况
        try{
            List<DepartmentRankVO> departmentRank= analysisService.getDepartmentRankVO(year, quarter);
            return departmentRank.isEmpty() ? Result.error(ResultCode.NOT_FOUND, "第"+year+"年的第"+quarter+"季度没有绩效数据") : Result.success(departmentRank);
        }catch (Exception e){
            log.error("查询失败", e);
            return Result.error(ResultCode.ERROR, "查询失败"+e.getMessage());
        }

    }

    @GetMapping("/employeeTrend")
    public Result<List<EmployeeTrendVO>> getEmployeeTrend(@RequestParam String empNo) {
        log.info("开始查询员工趋势... empNo = {}", empNo);
        try {
            List<EmployeeTrendVO> employeeTrend = analysisService.getEmployeeTrendVO(empNo);
            return employeeTrend.isEmpty() ? Result.error(ResultCode.NOT_FOUND, "没有该员工的绩效数据") : Result.success(employeeTrend);
        } catch (Exception e) {
            return Result.error(ResultCode.ERROR, "查询失败" + e.getMessage());
        }
    }

    @GetMapping("/departmentAvgScore")
    public Result<BigDecimal> getDepartmentAvgScore(@RequestParam Integer year, @RequestParam Integer quarter, @RequestParam String department) {
        log.info("开始查询部门平均分... year = {},quarter={},department={}", year, quarter, department);
        try {
            if (quarter < 1 || quarter > 4) {
                return Result.error(ResultCode.BAD_REQUEST, "季度必须在1-4之间");
            }
            BigDecimal departmentAvgScore = analysisService.getDepartmentAvgScore(year, quarter, department);
            return departmentAvgScore == null ? Result.error(ResultCode.NOT_FOUND, "没有该部门的绩效数据") : Result.success(departmentAvgScore);
        } catch (Exception e) {
            return Result.error(ResultCode.ERROR, "查询失败" + e.getMessage());
        }
    }
    @GetMapping("/companyAvgScore")
    public Result<BigDecimal> getCompanyAvgScore(@RequestParam Integer year, @RequestParam Integer quarter) {
        log.info("开始查询公司平均分... year = {},quarter={}", year, quarter);
        try{
            if(quarter < 1 || quarter > 4){
                return Result.error(ResultCode.BAD_REQUEST, "季度必须在1-4之间");
            }
            BigDecimal companyAvgScore = analysisService.getCompanyAvgScore(year, quarter);
            return companyAvgScore == null ? Result.error(ResultCode.NOT_FOUND, "没有该公司的绩效数据") : Result.success(companyAvgScore);
        }catch (Exception e){
            return Result.error(ResultCode.ERROR, "查询失败"+e.getMessage());
        }
    }

}
