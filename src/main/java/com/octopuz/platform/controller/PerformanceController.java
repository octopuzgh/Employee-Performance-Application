package com.octopuz.platform.controller;

import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.service.PerformanceService;
import com.octopuz.platform.vo.PerformanceVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/performances")
public class PerformanceController {
    @Resource
    private PerformanceService performanceService;
    @PostMapping
    public Result<PerformanceVO> addOrUpdate(@RequestBody Performance performance) {
        //验证季度必须在1-4之间
        if (performance.getQuarter() < 1 || performance.getQuarter() > 4) {
            return Result.error(ResultCode.ERROR, "季度必须在1-4之间");
        }
        //验证分数必须在0-100之间
        if (performance.getScore().compareTo(BigDecimal.ZERO) < 0 || performance.getScore().compareTo(BigDecimal.valueOf(100)) > 0) {
            return Result.error(ResultCode.ERROR, "分数必须在0-100之间");
        }

        return performanceService.addOrUpdate(performance) ? Result.success(performanceService.convertToVO( performance)) : Result.error(ResultCode.ERROR, "添加失败");
    }
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        return performanceService.removeById(id) ? Result.success("删除成功") : Result.error(ResultCode.ERROR, "删除失败");
    }
    @PutMapping
    public Result<PerformanceVO> update(@RequestBody Performance performance) {
        //id 不能为空
        if (performance.getId() == null) {
            return Result.error(ResultCode.ERROR, "id 不能为空");
        }
        return performanceService.updateById(performance) ? Result.success(performanceService.convertToVO( performance)) : Result.error(ResultCode.ERROR, "更新失败");
    }
    @GetMapping("/{id}")
    public Result<PerformanceVO> getById(@PathVariable Integer id) {
        return performanceService.getById(id) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(performanceService.convertToVO(performanceService.getById(id)));
    }
    @GetMapping("/emp/{empNo}")
    public Result<List<PerformanceVO>> getByEmpNo(@PathVariable String empNo) {
        return performanceService.getByEmpNo(empNo) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(performanceService.convertToVOList(performanceService.getByEmpNo(empNo)));
    }
    @GetMapping("/year/{year}")
    public Result<List<PerformanceVO>> getByYear(@PathVariable Integer year) {
        return performanceService.getByYear(year) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(performanceService.convertToVOList(performanceService.getByYear(year)));
    }

    @GetMapping("/year/{year}/quarter/{quarter}")
    public Result<List<PerformanceVO>> getByYearAndQuarter(@PathVariable Integer year, @PathVariable Integer quarter) {
        if (quarter < 1 || quarter > 4) {
            return Result.error(ResultCode.ERROR, "季度必须在1-4之间");
        }
        return performanceService.getByYearAndQuarter(year, quarter) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(performanceService.convertToVOList(performanceService.getByYearAndQuarter(year, quarter)));
    }
    //获取所有
    @GetMapping
    public Result<List<PerformanceVO>> list() {
        return performanceService.list() == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(performanceService.convertToVOList(performanceService.list()));
    }

}
