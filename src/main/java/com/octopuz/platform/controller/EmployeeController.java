package com.octopuz.platform.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.dto.EmployeeExcel;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.listener.EmployeeExcelListener;
import com.octopuz.platform.mapper.EmployeeMapper;
import com.octopuz.platform.service.impl.EmployeeServiceImpl;
import com.octopuz.platform.vo.EmployeeVO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
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
    private EmployeeServiceImpl employeeService;
    @Resource
    private EmployeeMapper employeeMapper;

    private EmployeeExcelListener excelListener = new EmployeeExcelListener(employeeService);

    @PostMapping
    public Result<EmployeeVO> add(@RequestBody Employee employee) {
        if (employee.getId() != null) {
            return Result.error(ResultCode.BAD_REQUEST, "员工ID必须为空");
        }
        return employeeService.save(employee) ? Result.success(employeeService.convertToVO(employee)) : Result.error(ResultCode.ERROR, "添加失败");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Integer id) {
        try {
            // 先检查是否存在
            Employee employee = employeeService.getById(id);
            if (employee == null) {
                return Result.error(ResultCode.NOT_FOUND, "员工不存在");
            }

            boolean removed = employeeService.removeById(id);
            if (removed) {
                return Result.success();
            } else {
                log.error("删除员工失败，ID: {}", id);
                return Result.error(ResultCode.ERROR, "删除失败");
            }
        } catch (Exception e) {
            log.error("删除员工异常，ID: {}", id, e);
            return Result.error(ResultCode.ERROR, "删除失败：" + e.getMessage());
        }
    }

    @PutMapping
    public Result<EmployeeVO> update(@RequestBody Employee employee) {
        if (employee.getId() == null) {
            return Result.error(ResultCode.BAD_REQUEST, "员工ID不能为空");
        }
        return employeeService.updateById(employee) ? Result.success(employeeService.convertToVO(employee)) : Result.error(ResultCode.ERROR, "更新失败");
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> getById(@PathVariable Integer id) {
        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return Result.error(ResultCode.NOT_FOUND, "员工不存在");
        }
        return employeeService.getById(id) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(employeeService.convertToVO(employeeService.getById(id)));
    }

    @GetMapping("/emp")
    public Result<EmployeeVO> getByEmpNo(@RequestParam String empNo) {
        Employee employee = employeeService.getByEmpNo(empNo);
        if (employee == null) {
            return Result.error(ResultCode.NOT_FOUND, "员工不存在");
        }
        return employeeService.getByEmpNo(empNo) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(employeeService.convertToVO(employeeService.getByEmpNo(empNo)));
    }

    @GetMapping("/name")
    public Result<EmployeeVO> getByName(@RequestParam String name) {
        Employee employee = employeeService.getByName(name);
        if (employee == null) {
            return Result.error(ResultCode.NOT_FOUND, "员工不存在");
        }
        return employeeService.getByName(name) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(employeeService.convertToVO(employeeService.getByName(name)));
    }

    @GetMapping("/page")
    public Result<Page<EmployeeVO>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10") Integer pageSize,
                                         @RequestParam(required = false) String department,
                                         @RequestParam(required = false) String position) {
        try {
            if (pageNum == null || pageNum <= 0) return Result.error(ResultCode.BAD_REQUEST, "页码不能小于1");
            if (pageSize == null || pageSize <= 0) return Result.error(ResultCode.BAD_REQUEST, "页大小不能小于1");
            if (pageSize > 100) return Result.error(ResultCode.BAD_REQUEST, "页大小不能大于100");
            if (pageNum > 10000) return Result.error(ResultCode.BAD_REQUEST, "页码不能大于10000");
            //查询条件（可选）
            LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(department)) queryWrapper.eq(Employee::getDepartment, department);
            if (StringUtils.hasText(position)) queryWrapper.eq(Employee::getPosition, position);
            //分页查询
            Page<Employee> page = employeeService.pageEmployees(pageNum, pageSize);
            Page<Employee> employeePage = employeeMapper.selectPage(page, queryWrapper);
            if (pageNum > page.getPages() && page.getTotal() > 0)
                return Result.error(ResultCode.NOT_FOUND, "页码超出范围，没有更多数据");
            return Result.success(employeeService.convertToVOPage(employeePage));
        } catch (Exception e) {
            return Result.error(ResultCode.ERROR, "查询失败" + e.getMessage());
        }


    }
    //导入员工数据
    @PostMapping("/import")
    public Result<Void> importExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST, "上传文件不能为空");
            }
            EasyExcel.read(file.getInputStream(), EmployeeExcel.class, excelListener)
                    .sheet()
                    .doRead();
            //检查
            if (!excelListener.getErrorList().isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST, "导入员工数据异常，请检查数据格式是否正确，并查看错误信息" + excelListener.getErrorList());
            }
            if (excelListener.getEmployees().isEmpty()) {
                return Result.error(ResultCode.BAD_REQUEST, "导入员工数据异常，请检查数据格式是否正确");
            }
            //保存
            boolean saved = excelListener.saveEmployees();
            if (!saved) {
                return Result.error(ResultCode.ERROR, "导入员工数据异常，请检查数据格式是否正确");
            } else {
                log.info("导入员工数据成功,共导入{}条", excelListener.getEmployees().size());
                return Result.success();
            }
        } catch (Exception e) {
            log.error("导入员工数据异常：{}", e.getMessage());
            return Result.error(ResultCode.ERROR, "导入员工数据异常" + e.getMessage());
        }
    }

    @GetMapping("/export")
    public void exportEmployees(HttpServletResponse response) {
        try {
            List<Employee> employees = employeeService.list();
            List<EmployeeExcel> employeeExcels = employeeService.convertTOExcelList(employees);
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
                    "attachment;filename=" + URLEncoder.encode("员工列表模板.xlsx", StandardCharsets.UTF_8) + ".xlsx");
            EasyExcel.write(response.getOutputStream(), EmployeeExcel.class)
                    .sheet("员工列表模板")
                    .doWrite(new ArrayList<EmployeeExcel>());
        } catch (Exception e) {
            log.error("导出员工数据模板异常：{}", e.getMessage());
        }
    }


}
