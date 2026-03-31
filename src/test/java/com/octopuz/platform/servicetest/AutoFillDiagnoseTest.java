package com.octopuz.platform.servicetest;

import com.octopuz.platform.entity.Employee;

import com.octopuz.platform.service.EmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

@SpringBootTest
public class AutoFillDiagnoseTest {

    @Autowired
    private EmployeeServiceImpl employeeService;

    @Test
    void testAutoFill() {
        System.out.println("=== 开始测试自动填充 ===");

        Employee employee = Employee.builder()
                .empNo("1116")
                .name("诊断测试")
                .department("技术部")
                .position("测试工程师")
                .hireDate(LocalDate.now())
                .email("diagnose@company.com")
                .build();

        System.out.println("保存前：");
        System.out.println("  createdAt: " + employee.getCreatedAt());
        System.out.println("  updatedAt: " + employee.getUpdatedAt());

        boolean saved = employeeService.save(employee);

        System.out.println("保存后：");
        System.out.println("  createdAt: " + employee.getCreatedAt());
        System.out.println("  updatedAt: " + employee.getUpdatedAt());
        System.out.println("  保存结果：" + saved);

        // 重新查询
        Employee queried = employeeService.getById(employee.getId());
        System.out.println("查询后：");
        System.out.println("  createdAt: " + queried.getCreatedAt());
        System.out.println("  updatedAt: " + queried.getUpdatedAt());
    }
}
