package com.octopuz.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.dto.PerformanceExcel;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.mapper.PerformanceMapper;
import com.octopuz.platform.service.interf.PerformanceService;
import com.octopuz.platform.vo.PerformanceVO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
@Service

public class PerformanceServiceImpl extends ServiceImpl<PerformanceMapper, Performance> implements PerformanceService {
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
    public boolean removeById(Integer id) {
        return super.removeById(id);
    }
    @Override
    @CacheEvict(value = {"analysis:rank","analysis:dept-avg","analysis:company-avg"}, allEntries = true)

    public boolean addOrUpdate(Performance performance) {
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getEmpNo, performance.getEmpNo())
                .eq(Performance::getYear, performance.getYear())
                .eq(Performance::getQuarter, performance.getQuarter());
        Performance one = this.getOne(queryWrapper);
        if (one == null) {
            return this.save(performance);
        }else {
            performance.setId(one.getId());
            return this.updateById(performance);
        }
    }
    @Override

    public PerformanceVO convertToVO(Performance performance){
        if(performance==null) return null;
        return PerformanceVO.builder()
                .id(performance.getId())
                .empNo(performance.getEmpNo())
                .year(performance.getYear())
                .quarter(performance.getQuarter())
                .score(performance.getScore())
                .build();
    }
    @Override
    public List<PerformanceVO> convertToVOList(List<Performance> performances){
        if(performances==null) return List.of();
        return performances.stream().map(this::convertToVO).toList();
    }
    @Override
    public List<PerformanceExcel> convertToExcelList(List<Performance> performances){
        if(performances==null) return List.of();
        return performances.stream().map(performance -> PerformanceExcel.builder()
                .empNo(performance.getEmpNo())
                .year(performance.getYear())
                .quarter(performance.getQuarter())
                .score(performance.getScore())
                .build()
                ).toList();
    }

}
