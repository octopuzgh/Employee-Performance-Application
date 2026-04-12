package com.octopuz.platform.servicetest;

import com.octopuz.platform.service.interf.PerformanceService;
import com.octopuz.platform.vo.PerformanceVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class PerformanceServiceImplTest {
    @Autowired
    PerformanceService performanceService;
    @Test
    void getByEmpNo() {
    }

    @Test
    void getByYear() {
    }

    @Test
    void getByYearAndQuarter() {
    }

    @Test
    void addOrUpdate() {
        PerformanceVO performance = new PerformanceVO();
        performance.setEmpNo("1001");
        performance.setYear(2022);
        performance.setQuarter(1);
        performance.setScore(new BigDecimal(90));
        PerformanceVO b = performanceService.addOrUpdatePerformance(performance);
        System.out.println( b);
    }
}