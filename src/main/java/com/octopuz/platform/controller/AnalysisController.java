package com.octopuz.platform.controller;

import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.service.AnalysisService;
import com.octopuz.platform.vo.DepartmentRankVO;
import com.octopuz.platform.vo.EmployeeTrendVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {
    @Resource
    private AnalysisService analysisService;
    @RequestMapping("/departmentRank")
    public Result<List<DepartmentRankVO>> getDepartmentRank(@RequestParam Integer year, @RequestParam Integer quarter) {
        //季度必须在1-4之间
        if (quarter < 1 || quarter > 4) {
            return Result.error(ResultCode.BAD_REQUEST, "季度必须在1-4之间");
        }
        //处理查询失败或查询结果为空的情况
        try{
            List<DepartmentRankVO> departmentRank= analysisService.getDepartmentRankVO(year, quarter);
            return departmentRank.isEmpty() ? Result.error(ResultCode.NOT_FOUND, "第"+year+"年的第"+quarter+"季度没有绩效数据") : Result.success(departmentRank);
        }catch (Exception e){
            return Result.error(ResultCode.ERROR, "查询失败"+e.getMessage());
        }

    }
    @RequestMapping("/employeeTrend")
    public Result<List<EmployeeTrendVO>> getEmployeeTrend(@RequestParam String empNo) {
        try {
            List<EmployeeTrendVO> employeeTrend = analysisService.getEmployeeTrendVO(empNo);
            return employeeTrend.isEmpty() ? Result.error(ResultCode.NOT_FOUND, "没有该员工的绩效数据") : Result.success(employeeTrend);
        }catch (Exception e){
            return Result.error(ResultCode.ERROR, "查询失败"+e.getMessage());
        }
    }
    @RequestMapping("/departmentAvgScore")
    public Result<BigDecimal> getDepartmentAvgScore(@RequestParam Integer year, @RequestParam Integer quarter, @RequestParam String department) {
        try{
            if(quarter < 1 || quarter > 4){
                return Result.error(ResultCode.BAD_REQUEST, "季度必须在1-4之间");
            }
            BigDecimal departmentAvgScore = analysisService.getDepartmentAvgScore(year, quarter, department);
            return departmentAvgScore == null ? Result.error(ResultCode.NOT_FOUND, "没有该部门的绩效数据") : Result.success(departmentAvgScore);
        }catch (Exception e){
            return Result.error(ResultCode.ERROR, "查询失败"+e.getMessage());
        }
    }
    @RequestMapping("/companyAvgScore")
    public Result<BigDecimal> getCompanyAvgScore(@RequestParam Integer year, @RequestParam Integer quarter) {
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
