package com.octopuz.platform.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.dto.PerformanceExcel;
import com.octopuz.platform.service.interf.PerformanceService;
import com.octopuz.platform.vo.PerformanceVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/performances")
public class PerformanceController {
    @Resource
    private PerformanceService performanceService;

    @PostMapping
    public Result<PerformanceVO> addOrUpdate(@RequestBody PerformanceVO performanceVO) {
        try {
            PerformanceVO result = performanceService.addOrUpdatePerformance(performanceVO);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("添加或更新绩效失败", e);
            return Result.error(ResultCode.ERROR, "操作失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        try {
            performanceService.deletePerformance(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("删除绩效失败，ID: {}", id, e);
            return Result.error(ResultCode.ERROR, "删除失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<PerformanceVO> update(@RequestBody PerformanceVO performanceVO) {
        try {
            PerformanceVO result = performanceService.updatePerformance(performanceVO);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("更新绩效失败", e);
            return Result.error(ResultCode.ERROR, "更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<PerformanceVO> getById(@PathVariable Integer id) {
        try {
            PerformanceVO performance = performanceService.getPerformanceById(id);
            return Result.success(performance);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("查询绩效失败，ID: {}", id, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/emp/{empNo}")
    public Result<List<PerformanceVO>> getByEmpNo(@PathVariable String empNo) {
        try {
            List<PerformanceVO> performances = performanceService.getPerformancesByEmpNo(empNo);
            return Result.success(performances);
        } catch (Exception e) {
            log.error("查询绩效失败，empNo: {}", empNo, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/year/{year}")
    public Result<List<PerformanceVO>> getByYear(@PathVariable Integer year) {
        try {
            List<PerformanceVO> performances = performanceService.getPerformancesByYear(year);
            return Result.success(performances);
        } catch (Exception e) {
            log.error("查询绩效失败，year: {}", year, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/year/{year}/quarter/{quarter}")
    public Result<List<PerformanceVO>> getByYearAndQuarter(@PathVariable Integer year, @PathVariable Integer quarter) {
        try {
            List<PerformanceVO> performances = performanceService.getPerformancesByYearAndQuarter(year, quarter);
            return Result.success(performances);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("查询绩效失败，year: {}, quarter: {}", year, quarter, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping
    public Result<List<PerformanceVO>> list() {
        try {
            List<PerformanceVO> performances = performanceService.getAllPerformances();
            return Result.success(performances);
        } catch (Exception e) {
            log.error("查询所有绩效失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @PostMapping("/import")
    public Result<Void> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            String error = performanceService.importExcel(file);
            return error == null ? Result.success() : Result.error(ResultCode.ERROR, error);
        } catch (Exception e) {
            log.error("导入绩效数据异常", e);
            return Result.error(ResultCode.ERROR, "导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportPerformances(HttpServletResponse response) {
        try {
            List<PerformanceVO> performances = performanceService.getAllPerformances();
            List<PerformanceExcel> performanceExcels = performanceService.convertToExcelList(performances);

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("员工绩效", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), PerformanceExcel.class)
                    .sheet("员工绩效")
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .doWrite(performanceExcels);

            log.info("导出员工绩效成功，数据量：{}", performances.size());
        } catch (Exception e) {
            log.error("导出员工绩效异常：{}", e.getMessage());
        }
    }

    @GetMapping("/template")
    public void exportTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("员工绩效模板", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

            EasyExcel.write(response.getOutputStream(), PerformanceExcel.class)
                    .sheet("员工绩效模板")
                    .doWrite(new ArrayList<PerformanceExcel>());
        } catch (Exception e) {
            log.error("导出员工绩效模板异常：{}", e.getMessage());
        }
    }
}
