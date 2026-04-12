package com.octopuz.platform.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.dto.EmployeeExcel;
import com.octopuz.platform.service.interf.EmployeeService;
import com.octopuz.platform.vo.EmployeeVO;
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
@RequestMapping("/api/employees")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService;
//    @Resource
//    private EmployeeMapper employeeMapper;


    @PostMapping
    public Result<EmployeeVO> add(@RequestBody EmployeeVO employeeVO) {
        try {
            EmployeeVO result = employeeService.addEmployee(employeeVO);
            return Result.success(result);
        } catch (Exception e) {
            log.error("添加员工失败", e);
            return Result.error(ResultCode.ERROR, "添加失败：" + e.getMessage());
        }}

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        try {
            employeeService.deleteEmployee(id);
            return Result.success();
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("删除员工失败，ID: {}", id, e);
            return Result.error(ResultCode.ERROR, "删除失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<EmployeeVO> update(@RequestBody EmployeeVO employeeVO) {
        try {
            EmployeeVO result = employeeService.updateEmployee(employeeVO);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("更新员工失败", e);
            return Result.error(ResultCode.ERROR, "更新失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> getById(@PathVariable Integer id) {
        try {
            EmployeeVO employee = employeeService.getEmployeeById(id);
            return Result.success(employee);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("查询员工失败，ID: {}", id, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/emp")
    public Result<EmployeeVO> getByEmpNo(@RequestParam String empNo) {
        try {
            EmployeeVO employee = employeeService.getEmployeeByEmpNo(empNo);
            return Result.success(employee);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("查询员工失败，empNo: {}", empNo, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/name")
    public Result<EmployeeVO> getByName(@RequestParam String name) {
        try {
            EmployeeVO employee = employeeService.getEmployeeByName(name);
            return Result.success(employee);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.NOT_FOUND, e.getMessage());
        } catch (Exception e) {
            log.error("查询员工失败，name: {}", name, e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }
    @GetMapping("/search")
    public Result<List<EmployeeVO>> searchByName(@RequestParam String name) {
        try {
            List<EmployeeVO> employees = employeeService.searchEmployeesByName(name);
            return Result.success(employees);
        } catch (Exception e) {
            log.error("搜索员工失败，name: {}", name, e);
            return Result.error(ResultCode.ERROR, "搜索失败：" + e.getMessage());
        }
    }

    @GetMapping("/page")
    public Result<Page<EmployeeVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String department,
                                         @RequestParam(required = false) String position) {
        try {
            Page<EmployeeVO> result = employeeService.pageEmployees(pageNum, pageSize, department, position);
            return Result.success(result);
        } catch (IllegalArgumentException e) {
            return Result.error(ResultCode.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("分页查询失败", e);
            return Result.error(ResultCode.ERROR, "查询失败：" + e.getMessage());
        }
    }
    //导入员工数据
    @PostMapping("/import")
    public Result<Void> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            String error = employeeService.importExcel(file);
            return error == null ? Result.success() : Result.error(ResultCode.ERROR, error);
        } catch (Exception e) {
            log.error("导入员工数据异常", e);
            return Result.error(ResultCode.ERROR, "导入失败：" + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportEmployees(HttpServletResponse response) {
        try {
            List<EmployeeVO> employees = employeeService.getAllEmployees();
            List<EmployeeExcel> employeeExcels = employeeService.convertToExcelList(employees);
            //设置响应头

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("员工列表_" + System.currentTimeMillis(), StandardCharsets.UTF_8);
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), EmployeeExcel.class)
                    .sheet("员工列表")
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .doWrite(employeeExcels);
        } catch (Exception e) {
            log.error("导出员工数据异常：{}", e.getMessage());
        }
    }

    @GetMapping("/template")
    public void exportTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode("员工列表模板", StandardCharsets.UTF_8) + ".xlsx");
            EasyExcel.write(response.getOutputStream(), EmployeeExcel.class)
                    .sheet("员工列表模板")
                    .doWrite(new ArrayList<EmployeeExcel>());
        } catch (Exception e) {
            log.error("导出员工数据模板异常：{}", e.getMessage());
        }
    }


}
