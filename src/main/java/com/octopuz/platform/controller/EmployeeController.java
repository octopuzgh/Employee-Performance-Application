package com.octopuz.platform.controller;

import com.octopuz.platform.common.Result;
import com.octopuz.platform.common.ResultCode;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.service.EmployeeService;
import jakarta.annotation.Resource;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees")
public class EmployeeController {
    @Resource
    private EmployeeService employeeService;
    @PostMapping
    public Result<Employee> add(@RequestBody Employee employee){
        return employeeService.save(employee) ? Result.success(employee) : Result.error(ResultCode.ERROR,"添加失败");
    }
    @DeleteMapping("/{id}")
    public Result<Employee> delete(@PathVariable Integer id){
        return employeeService.removeById(id) ? Result.success() : Result.error(ResultCode.ERROR,"删除失败");
    }
    @PutMapping
    public Result<Employee> update(@RequestBody Employee employee){
        return employeeService.updateById(employee) ? Result.success(employee) : Result.error(ResultCode.ERROR,"更新失败");
    }
    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable Integer id){
        return employeeService.getById(id) == null ? Result.error(ResultCode.ERROR,"查询失败") : Result.success(employeeService.getById(id));
    }



}
