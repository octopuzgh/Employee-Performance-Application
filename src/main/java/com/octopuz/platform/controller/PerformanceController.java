package com.octopuz.platform.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.dto.EmployeeExcel;
import com.octopuz.platform.dto.PerformanceExcel;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.listener.PerformanceExcelListener;
import com.octopuz.platform.service.impl.PerformanceServiceImpl;
import com.octopuz.platform.vo.PerformanceVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/performances")
public class PerformanceController {
    @Resource
    private PerformanceServiceImpl performanceService;
    PerformanceExcelListener performanceExcelListener=new PerformanceExcelListener();

    @PostMapping
    public Result<PerformanceVO> addOrUpdate(@RequestBody Performance performance) {
        log.info("开始添加或更新员工绩效... performance = {}", performance);
        //验证季度必须在1-4之间
        if (performance.getQuarter() < 1 || performance.getQuarter() > 4) {
            return Result.error(ResultCode.ERROR, "季度必须在1-4之间");
        }
        //验证分数必须在0-100之间
        if (performance.getScore().compareTo(BigDecimal.ZERO) < 0 || performance.getScore().compareTo(BigDecimal.valueOf(100)) > 0) {
            return Result.error(ResultCode.ERROR, "分数必须在0-100之间");
        }

        return performanceService.addOrUpdate(performance) ? Result.success(performanceService.convertToVO(performance)) : Result.error(ResultCode.ERROR, "添加失败");
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
        return performanceService.updateById(performance) ? Result.success(performanceService.convertToVO(performance)) : Result.error(ResultCode.ERROR, "更新失败");
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
    // 批量导入
    @PostMapping("/import")
    public Result<Void> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            String error = performanceService.importExcel(file);
            return error == null ? Result.success() : Result.error(ResultCode.ERROR, error);
        } catch (Exception e) {
            log.error("导入员工数据异常：{}", e.getMessage());
            return Result.error(ResultCode.ERROR, "导入员工数据异常"+e.getMessage());
        }
    }
    @GetMapping("/export")
    public void exportPerformances(HttpServletResponse response) {
        try {
            List<Performance> performances = performanceService.list();
            List<PerformanceExcel> performanceExcels = performanceService.convertToExcelList(performances);
            //设置响应头
            response.setContentType( "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("员工绩效", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename="+fileName+".xlsx");
            //导出excel
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
        try{
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("员工绩效模板", StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), PerformanceExcel.class)
                    .sheet("员工绩效模板")
                    .doWrite(new ArrayList<EmployeeExcel>());
        }catch (Exception e){
            log.error("导出员工绩效模板异常：{}", e.getMessage());
        }
    }

}
