package com.octopuz.platform.servicetest;

import com.octopuz.platform.service.AnalysisServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class AnalysisServiceImplTest {
    @Autowired
    AnalysisServiceImpl performanceServiceImpl;
    @Test
    void getDepartmentRanK() {
        List<Map<String, Object>> departmentRank = performanceServiceImpl.getDepartmentRank(2023, 1);
        //打印排名+成绩
        int i = 1;
        for (Map<String, Object> map : departmentRank) {
            System.out.println(i+" "+map);
            i++;
        }

    }

    @Test
    void getEmployeeTrend() {
    }

    @Test
    void getDepartmentAvgScore() {
    }

    @Test
    void getCompanyAvgScore() {
    }
}