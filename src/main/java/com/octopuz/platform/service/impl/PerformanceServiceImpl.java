package com.octopuz.platform.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.converter.PerformanceConverter;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service

@Slf4j
public class PerformanceServiceImpl extends ServiceImpl<PerformanceMapper, Performance> implements PerformanceService {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private EmployeeService employeeService;
    @Resource
    private KafkaSender kafkaSender;
    @Resource
    private PerformanceConverter performanceConverter;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PerformanceVO addOrUpdatePerformance(PerformanceVO performanceVO) {
        //检查参数逻辑
        if (performanceVO.getQuarter() < 1 || performanceVO.getQuarter() > 4) {
            throw new IllegalArgumentException("季度必须在1-4之间");
        }
        if (performanceVO.getScore().compareTo(BigDecimal.ZERO) < 0 ||
            performanceVO.getScore().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("分数必须在0-100之间");
        }
        //检查是否存在记录
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getEmpNo, performanceVO.getEmpNo())
                .eq(Performance::getYear, performanceVO.getYear())
                .eq(Performance::getQuarter, performanceVO.getQuarter());
        Performance existing = getOne(queryWrapper);

        Performance performance = performanceConverter.toEntity(performanceVO);

        boolean success;
        if (existing == null) {
            success = save(performance);
            if (success) {
                kafkaSender.sendOperationLog("ADD_PERFORMANCE",
                        String.format("添加了员工%s的%d年%d季度的绩效数据",
                                performance.getEmpNo(), performance.getYear(), performance.getQuarter()));
            }
        } else {
            performance.setId(existing.getId());
            success = updateById(performance);
            if (success) {
                kafkaSender.sendOperationLog("UPDATE_PERFORMANCE",
                        String.format("更新了员工%s的%d年%d季度的绩效数据",
                                performance.getEmpNo(), performance.getYear(), performance.getQuarter()));
            }
        }

        if (!success) {
            throw new RuntimeException("操作失败");
        }

        return performanceConverter.toVO(performance);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePerformance(Integer id) {
        //检查参数逻辑
        Performance performance = getById(id);
        if (performance == null) {
            throw new IllegalArgumentException("绩效记录不存在");
        }

        boolean removed = removeById(id);
        if (!removed) {
            throw new RuntimeException("删除失败");
        }

        kafkaSender.sendOperationLog("DELETE_PERFORMANCE",
                String.format("删除了员工%s的%d年%d季度的绩效数据",
                        performance.getEmpNo(), performance.getYear(), performance.getQuarter()));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PerformanceVO updatePerformance(PerformanceVO performanceVO) {
        //检查参数逻辑
        if (performanceVO.getId() == null) {
            throw new IllegalArgumentException("ID不能为空");
        }
        //检查是否存在记录
        Performance existing = getById(performanceVO.getId());
        if (existing == null) {
            throw new IllegalArgumentException("绩效记录不存在");
        }

        Performance performance = performanceConverter.toEntity(performanceVO);

        boolean updated = updateById(performance);
        if (!updated) {
            throw new RuntimeException("更新失败");
        }

        kafkaSender.sendOperationLog("UPDATE_PERFORMANCE",
                String.format("更新了员工%s的%d年%d季度的绩效数据",
                        performance.getEmpNo(), performance.getYear(), performance.getQuarter()));

        return performanceConverter.toVO(performance);
    }

    @Override
    public PerformanceVO getPerformanceById(Integer id) {
        Performance performance = getById(id);
        if (performance == null) {
            throw new IllegalArgumentException("绩效记录不存在");
        }
        return performanceConverter.toVO(performance);
    }

    @Override
    public List<PerformanceVO> getPerformancesByEmpNo(String empNo) {
        LambdaQueryWrapper<Performance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Performance::getEmpNo, empNo)
                .orderByDesc(Performance::getYear)
                .orderByDesc(Performance::getQuarter);
        List<Performance> performances = list(wrapper);
        return performanceConverter.toVOList(performances);
    }

    @Override
    public List<PerformanceVO> getPerformancesByYear(Integer year) {
        LambdaQueryWrapper<Performance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Performance::getYear, year)
                .orderByDesc(Performance::getQuarter);
        List<Performance> performances = list(wrapper);
        return performanceConverter.toVOList(performances);
    }




    @Override
    public List<PerformanceVO> getPerformancesByYearAndQuarter(Integer year, Integer quarter) {
        //检查参数逻辑
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("季度必须在1-4之间");
        }

        LambdaQueryWrapper<Performance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Performance::getYear, year)
                .eq(Performance::getQuarter, quarter);
        List<Performance> performances = list(wrapper);
        return performanceConverter.toVOList(performances);
    }


    @Override
    public List<PerformanceVO> getAllPerformances() {
        List<Performance> performances = list();
        return performanceConverter.toVOList(performances);
    }



    @Override
    public List<PerformanceExcel> convertToExcelList(List<PerformanceVO> performances) {
        if (performances == null) return List.of();
        return performances.stream().map(vo -> PerformanceExcel.builder()
                .empNo(vo.getEmpNo())
                .year(vo.getYear())
                .quarter(vo.getQuarter())
                .score(vo.getScore())
                .build()).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importExcel(MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return "文件为空";
            }

            String lockKey = "lock:import-excel-lock:performance";
            RLock lock = redissonClient.getLock(lockKey);
            boolean isLocked = lock.tryLock(0, 300, TimeUnit.SECONDS);
            if (!isLocked) {
                return "请勿重复导入";
            }

            try {
                PerformanceExcelListener listener = new PerformanceExcelListener(employeeService);
                EasyExcel.read(file.getInputStream(), PerformanceExcel.class, listener)
                        .sheet()
                        .doRead();

                if (!listener.getErrorList().isEmpty()) {
                    return "导入失败" + listener.getErrorList();
                }
                if (listener.getPerformances().isEmpty()) {
                    return "导入失败";
                }

                boolean saved = saveBatch(listener.getPerformances());
                if (!saved) {
                    return "导入失败";
                }

                int size = listener.getPerformances().size();
                log.info("导入成功,共导入{}条", size);
                kafkaSender.sendOperationLog("IMPORT_PERFORMANCE",
                        String.format("导入了%d条员工绩效数据", size));
                return null;
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("导入失败", e);
            return "导入失败" + e.getMessage();
        }
    }

}
