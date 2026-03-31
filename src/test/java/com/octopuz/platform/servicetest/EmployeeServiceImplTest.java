package com.octopuz.platform.servicetest;

import com.octopuz.platform.service.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmployeeServiceImplTest {

    @Autowired
    private EmployeeServiceImpl employeeService;
    @Test
    void getByDepartment() {

        String department = "技术部" ;
        employeeService.getByDepartment(department).forEach(System.out::println);

    }
}
