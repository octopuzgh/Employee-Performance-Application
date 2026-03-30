package com.octopuz.platform.service;

import com.octopuz.platform.entity.Performance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PerformanceServiceImplTest {
    @Autowired
    PerformanceServiceImpl performanceServiceImpl;
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
        Performance performance = new Performance();
        performance.setEmpNo("1001");
        performance.setYear(2022);
        performance.setQuarter(1);
        performance.setScore(new BigDecimal(90));
        boolean b = performanceServiceImpl.addOrUpdate(performance);
        assertTrue(b,"操作成功");
    }
}