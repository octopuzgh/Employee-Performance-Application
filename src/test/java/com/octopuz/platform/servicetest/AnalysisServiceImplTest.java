package com.octopuz.platform.servicetest;

import com.octopuz.platform.service.impl.AnalysisServiceImpl;
import com.octopuz.platform.vo.DepartmentRankVO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class AnalysisServiceImplTest {
    @Autowired
    AnalysisServiceImpl analysisService;
    @Test
    void getDepartmentRanK() {
        List<DepartmentRankVO> departmentRank = analysisService.getDepartmentRankVO(2023, 1);
        //打印排名+成绩
        int i = 1;
        for (DepartmentRankVO map : departmentRank) {
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