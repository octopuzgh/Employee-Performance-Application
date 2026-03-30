package com.octopuz.platform.mappertest;

import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.mapper.EmployeeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest

public class EmployeeMapperTest {
    @Autowired
    private EmployeeMapper employeeMapper;
    @Test
    void zzselectAll() {
        List<Employee> employees = employeeMapper.selectList(null);
        employees.forEach(System.out::println);
    }
    @Test
    void selectById() {
        System.out.println(employeeMapper.selectById(1));
    }
}
