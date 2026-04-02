package com.octopuz.platform.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.octopuz.platform.dto.PerformanceExcel;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.service.impl.PerformanceServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.util.ArrayList;

@Slf4j
@Data
@AllArgsConstructor
public class PerformanceExcelListener extends AnalysisEventListener<PerformanceExcel> {
    private final PerformanceServiceImpl performanceService;
    private final ArrayList<Performance> performances = new ArrayList<>();
    private final ArrayList<String> errorList = new ArrayList<>();

    @Override
    public void invoke(PerformanceExcel data, AnalysisContext context) {
        try {
            if (data.getEmpNo() == null || data.getEmpNo().isEmpty()) {
                errorList.add("第" + (context.readRowHolder().getRowIndex() + 1) + "行数据有误，请检查数据！");
                return;
            } else if (data.getYear() == null || data.getYear() < 1950 || data.getYear() > 2100) {
                errorList.add("第" + (context.readRowHolder().getRowIndex() + 1) + "行数据有误，请检查数据！");
                return;
            } else if (data.getQuarter() == null || data.getQuarter() <= 0 || data.getQuarter() > 4) {
                errorList.add("第" + (context.readRowHolder().getRowIndex() + 1) + "行数据有误，请检查数据！（季度必须在1-4之间）");
                return;
            } else if (data.getScore() == null || data.getScore().compareTo(BigDecimal.ZERO) < 0) {
                errorList.add("第" + (context.readRowHolder().getRowIndex() + 1) + "行数据有误，请检查数据！（绩效必须在0-100之间）");
                return;
            }
            Performance performance = Performance.builder()
                    .empNo(data.getEmpNo())
                    .year(data.getYear())
                    .quarter(data.getQuarter())
                    .score(data.getScore())
                    .build();
            performances.add(performance);

        } catch (Exception e) {
            log.error("数据导入失败：{}", e.getMessage());
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        log.info("所有数据解析完毕，共解析{}条数据", performances.size());
    }
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    public boolean savePerformances() {
        if (performances.isEmpty()) {
            return false;
        }
        return performanceService.saveOrUpdateBatch(performances);
    }
}
