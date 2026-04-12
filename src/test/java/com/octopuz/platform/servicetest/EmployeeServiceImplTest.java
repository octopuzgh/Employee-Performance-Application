package com.octopuz.platform.servicetest;

import com.octopuz.platform.service.impl.EmployeeServiceImpl;
import com.octopuz.platform.service.interf.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmployeeServiceImplTest {

    @Autowired
    private EmployeeService employeeService;
    @Test
    void getByDepartment() {

        String department = "技术部" ;
        employeeService.getEmployeesByDepartment(department).forEach(System.out::println);

    }
}
