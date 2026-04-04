package com.octopuz.platform.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.dto.PerformanceExcel;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.listener.PerformanceExcelListener;
import com.octopuz.platform.mapper.PerformanceMapper;
import com.octopuz.platform.service.interf.EmployeeService;
import com.octopuz.platform.service.interf.PerformanceService;
import com.octopuz.platform.utils.KafkaSender;
import com.octopuz.platform.vo.PerformanceVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service

@Slf4j
public class PerformanceServiceImpl extends ServiceImpl<PerformanceMapper, Performance> implements PerformanceService {
    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private KafkaSender kafkaSender;

    @Override
    public List<Performance> getByEmpNo(String empNo) {
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getEmpNo, empNo)
                .orderByDesc(Performance::getYear)
                .orderByDesc(Performance::getQuarter);
        return this.list(queryWrapper);
    }

    @Override
    public List<Performance> getByYear(Integer year) {
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getYear, year)
                .orderByDesc(Performance::getQuarter);
        return this.list(queryWrapper);
    }

    @Override
    public List<Performance> getByYearAndQuarter(Integer year, Integer quarter) {
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getYear, year)
                .eq(Performance::getQuarter, quarter);
        return this.list(queryWrapper);
    }

    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    @Override
    public boolean updateById(Performance performance) {
        Performance oldPerformance = this.getById(performance.getId());
        if (oldPerformance == null) {
            return false;
        }
        boolean updated = super.updateById(performance);
        if(updated){
            kafkaSender.sendOperationLog("UPDATE_PERFORMANCE",
                    String.format("更新了员工%s的%d年%d季度的绩效数据",
                            performance.getEmpNo(), performance.getYear(), performance.getQuarter()));
        }
        return updated;
    }

    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    @Override
    public boolean removeById(Integer id) {
        Performance performance = this.getById(id);
        if (performance == null) {
            return false;
        }
        boolean removed = super.removeById(id);
        if (removed) {
            kafkaSender.sendOperationLog("DELETE_PERFORMANCE",
                    String.format("删除了员工%s的%d年%d季度的绩效数据",
                            performance.getEmpNo(), performance.getYear(), performance.getQuarter()));
        }
        return removed;
    }

    @Override
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)

    public boolean addOrUpdate(Performance performance) {
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getEmpNo, performance.getEmpNo())
                .eq(Performance::getYear, performance.getYear())
                .eq(Performance::getQuarter, performance.getQuarter());
        Performance one = this.getOne(queryWrapper);
        if (one == null) {
            boolean added = super.save(performance);
            if (added) {
                kafkaSender.sendOperationLog("ADD_PERFORMANCE",
                        String.format("添加了员工%s的%d年%d季度的绩效数据",
                                performance.getEmpNo(), performance.getYear(), performance.getQuarter()));
            }
            return added;
        } else {
            performance.setId(one.getId());
            boolean updated = super.updateById(performance);
            if (updated) {
                kafkaSender.sendOperationLog("UPDATE_PERFORMANCE",
                        String.format("更新了员工%s的%d年%d季度的绩效数据",
                                performance.getEmpNo(), performance.getYear(), performance.getQuarter()));
            }
            return updated;
        }
    }

    @Override

    public PerformanceVO convertToVO(Performance performance) {
        if (performance == null) return null;
        return PerformanceVO.builder()
                .id(performance.getId())
                .empNo(performance.getEmpNo())
                .year(performance.getYear())
                .quarter(performance.getQuarter())
                .score(performance.getScore())
                .build();
    }

    @Override
    public List<PerformanceVO> convertToVOList(List<Performance> performances) {
        if (performances == null) return List.of();
        return performances.stream().map(this::convertToVO).toList();
    }

    @Override
    public List<PerformanceExcel> convertToExcelList(List<Performance> performances) {
        if (performances == null) return List.of();
        return performances.stream().map(performance -> PerformanceExcel.builder()
                .empNo(performance.getEmpNo())
                .year(performance.getYear())
                .quarter(performance.getQuarter())
                .score(performance.getScore())
                .build()
        ).toList();
    }

    @Override
    @CacheEvict(value = {"analysis:rank", "analysis:dept-avg", "analysis:company-avg"}, allEntries = true)
    public String importExcel(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "文件为空";
            }
            String lockKey = "lock:import-excel-lock:performance";
            RLock lock = redissonClient.getLock(lockKey);
            boolean isLocked = lock.tryLock();
            if (!isLocked) {
                return "请勿重复导入";
            }
            try {
                PerformanceExcelListener performanceExcelListener = new PerformanceExcelListener(employeeService);
                EasyExcel.read(file.getInputStream(), PerformanceExcel.class, performanceExcelListener)
                        .sheet()
                        .doRead();
                if (!performanceExcelListener.getErrorList().isEmpty()) {
                    return "导入失败" + performanceExcelListener.getErrorList();
                }
                if (performanceExcelListener.getPerformances().isEmpty()) {
                    return "导入失败";
                }
                boolean saved = super.saveBatch(performanceExcelListener.getPerformances(), performanceExcelListener.getPerformances().size());

                if (saved) {
                    int size = performanceExcelListener.getPerformances().size();
                    log.info("导入成功,共导入{}条", performanceExcelListener.getPerformances().size());
                    kafkaSender.sendOperationLog("IMPORT_PERFORMANCE",
                            String.format("导入了%d条员工绩效数据", size));
                    return null;
                } else {
                    return "导入失败";
                }
            } finally {
                if(lock.isHeldByCurrentThread()){
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.info("导入失败", e);

            return "导入失败" + e.getMessage();
        }
    }

}
