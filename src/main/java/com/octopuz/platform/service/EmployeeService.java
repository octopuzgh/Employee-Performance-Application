package com.octopuz.platform.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.vo.EmployeeVO;

import java.util.List;

public interface EmployeeService extends IService<Employee> {
    //分页查询
    Page<Employee> pageEmployees(Integer pageNum, Integer pageSize);
    //按部门查询
    List<Employee> getByDepartment(String department);
    //按工号查询
    Employee getByEmpNo(String empNo);
    //转换为VO
    EmployeeVO convertToVO(Employee employee);
    List<EmployeeVO> convertToVOList(List<Employee> employees);
    Page<EmployeeVO> convertToVOPage(Page<Employee> page);
}
