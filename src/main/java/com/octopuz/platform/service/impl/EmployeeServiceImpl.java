package com.octopuz.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.mapper.EmployeeMapper;
import com.octopuz.platform.service.interf.EmployeeService;
import com.octopuz.platform.vo.EmployeeVO;
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
    @Override
    public EmployeeVO convertToVO(Employee employee){
        if(employee==null) return null;
        return EmployeeVO.builder()
                .id(employee.getId())
                .empNo(employee.getEmpNo())
                .name(employee.getName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .hireDate(employee.getHireDate())
                .email(employee.getEmail())
                .build();
    }
    @Override
    public  List<EmployeeVO> convertToVOList(List<Employee> employees){
        if(employees==null) return List.of();
        return employees.stream().map(this::convertToVO).toList();
    }
    @Override
    public Page<EmployeeVO> convertToVOPage(Page<Employee> page){
        if(page==null) return null;
        Page<EmployeeVO> employeeVOPage = new Page<>();
        employeeVOPage.setCurrent(page.getCurrent());
        employeeVOPage.setSize(page.getSize());
        employeeVOPage.setTotal(page.getTotal());
        employeeVOPage.setRecords(convertToVOList(page.getRecords()));
        return employeeVOPage;
    }
}
