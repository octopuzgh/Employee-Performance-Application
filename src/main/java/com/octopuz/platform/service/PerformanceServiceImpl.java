package com.octopuz.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.mapper.PerformanceMapper;
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

    @Override
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
}
