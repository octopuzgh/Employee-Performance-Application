package com.octopuz.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.octopuz.platform.entity.Employee;

import java.util.List;

public interface EmployeeService extends IService<Employee> {
    Page<Employee> pageEmployees(Integer pageNum, Integer pageSize);
    List<Employee> getByDepartment(String department);
    Employee getByEmpNo(String empNo);
}
