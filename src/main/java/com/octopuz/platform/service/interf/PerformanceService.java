package com.octopuz.platform.service.interf;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopuz.platform.dto.PerformanceExcel;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.vo.PerformanceVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PerformanceService extends IService<Performance> {

    @CacheEvict(value = {
            "analysis:rank",
            "analysis:trend",
            "analysis:dept-stats",
            "analysis:emp-rank",
            "analysis:dept-avg",
            "analysis:company-avg",
            "analysis:company-summary",
            "analysis:anomaly-detect"
    }, allEntries = true)
    PerformanceVO addOrUpdatePerformance(PerformanceVO performanceVO);

    @CacheEvict(value = {
            "analysis:rank",
            "analysis:trend",
            "analysis:dept-stats",
            "analysis:emp-rank",
            "analysis:dept-avg",
            "analysis:company-avg",
            "analysis:company-summary",
            "analysis:anomaly-detect"
    }, allEntries = true)void deletePerformance(Integer id);

    @CacheEvict(value = {
            "analysis:rank",
            "analysis:trend",
            "analysis:dept-stats",
            "analysis:emp-rank",
            "analysis:dept-avg",
            "analysis:company-avg",
            "analysis:company-summary",
            "analysis:anomaly-detect"
    }, allEntries = true)PerformanceVO updatePerformance(PerformanceVO performanceVO);

    PerformanceVO getPerformanceById(Integer id);

    List<PerformanceVO> getPerformancesByEmpNo(String empNo);

    List<PerformanceVO> getPerformancesByYear(Integer year);

    List<PerformanceVO> getPerformancesByYearAndQuarter(Integer year, Integer quarter);

    List<PerformanceVO> getAllPerformances();

    // Excel 相关
    List<PerformanceExcel> convertToExcelList(List<PerformanceVO> performances);

    @CacheEvict(value = {
            "analysis:rank",
            "analysis:trend",
            "analysis:dept-stats",
            "analysis:emp-rank",
            "analysis:dept-avg",
            "analysis:company-avg",
            "analysis:company-summary",
            "analysis:anomaly-detect"
    }, allEntries = true)String importExcel(MultipartFile file);
}
