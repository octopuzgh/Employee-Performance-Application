package com.octopuz.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.mapper.EmployeeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    @Override
    public Page<Employee> pageEmployees(Integer pageNum, Integer pageSize){
        return this.page(new Page<>(pageNum, pageSize));
    }

    @Override
    public List<Employee> getByDepartment(String department){
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getDepartment, department);
        return this.list(employeeLambdaQueryWrapper);

    }
    @Override
    public Employee getByEmpNo(String empNo){
        LambdaQueryWrapper<Employee> employeeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        employeeLambdaQueryWrapper.eq(Employee::getEmpNo, empNo);
        return this.getOne(employeeLambdaQueryWrapper);


    }
}
