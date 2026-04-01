package com.octopuz.platform.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.mapper.EmployeeMapper;
import com.octopuz.platform.service.impl.EmployeeServiceImpl;
import com.octopuz.platform.vo.EmployeeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Resource
    private EmployeeServiceImpl employeeService;
    @Resource
    private EmployeeMapper employeeMapper;

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
        return employeeService.updateById(employee) ? Result.success( employeeService.convertToVO(employee)) : Result.error(ResultCode.ERROR, "更新失败");
    }

    @GetMapping("/{id}")
    public Result<EmployeeVO> getById(@PathVariable Integer id) {
        Employee employee = employeeService.getById(id);
        if (employee == null) {
            return Result.error(ResultCode.NOT_FOUND, "员工不存在");
        }
        return employeeService.getById(id) == null ? Result.error(ResultCode.ERROR, "查询失败") : Result.success(employeeService.convertToVO(employeeService.getById(id)));
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
            if(StringUtils.hasText(department)) queryWrapper.eq(Employee::getDepartment, department);
            if(StringUtils.hasText( position)) queryWrapper.eq(Employee::getPosition, position);
            //分页查询
            Page<Employee> page = employeeService.pageEmployees(pageNum, pageSize);
            Page<Employee> employeePage = employeeMapper.selectPage(page, queryWrapper);
            if(pageNum>page.getPages()&&page.getTotal()>0) return Result.error(ResultCode.NOT_FOUND, "页码超出范围，没有更多数据");
            return Result.success( employeeService.convertToVOPage(employeePage));
        } catch (Exception e) {
            return Result.error(ResultCode.ERROR, "查询失败" + e.getMessage());
        }


    }


}
