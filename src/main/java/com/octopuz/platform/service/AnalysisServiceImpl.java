package com.octopuz.platform.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.mapper.EmployeeMapper;
import com.octopuz.platform.mapper.PerformanceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalysisServiceImpl extends ServiceImpl<PerformanceMapper, Performance> implements AnalysisService {
    @Autowired
    private PerformanceMapper performanceMapper;
    @Autowired
    private EmployeeMapper employeeMapper ;
    @Override
    public List<Map<String, Object>> getDepartmentRanK(Integer year, Integer quarter) {
        //查询季度所有绩效
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getYear,year)
                    .eq(Performance::getQuarter,quarter);
        List<Performance> performances = performanceMapper.selectList(queryWrapper);
        //查询所有员工
        List<Employee> employees = employeeMapper.selectList(null);
        Map<String, Employee> employeeMap = employees.stream().collect(Collectors.toMap(Employee::getEmpNo, e -> e));
        //按部门分组算平均分
        //部门绩效
        Map<String, List<Performance>> departmentPerformance = performances.stream()
                .filter(p -> employeeMap.containsKey(p.getEmpNo()))
                .collect(Collectors.groupingBy(p -> employeeMap.get(p.getEmpNo()).getDepartment()));
        //部门平均分排名
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Performance>> entry : departmentPerformance.entrySet()){
            Map<String, Object> departmentPerformanceMap = new HashMap<>();
            String key = entry.getKey();
            List<Performance> value = entry.getValue();
            BigDecimal avgScore = value.stream()
                    .map(Performance::getScore)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(value.size()), 2, RoundingMode.HALF_UP);
            departmentPerformanceMap.put("department",key);
            departmentPerformanceMap.put("avgScore",avgScore);
            departmentPerformanceMap.put("count",value.size());
            result.add(departmentPerformanceMap);
        }
        result.sort((o1, o2) -> ((BigDecimal)o2.get("avgScore")).compareTo((BigDecimal)o1.get("avgScore")));
        return result;
    }

    @Override
    public List<Map<String, Object>> getEmployeeTrend(String empNo) {
        //按工号获得绩效
        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getEmpNo,empNo)
                .orderByAsc(Performance::getYear,Performance::getQuarter);
        List<Performance> performances = performanceMapper.selectList(queryWrapper);
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        for (Performance performance : performances){
            HashMap<String, Object> item = new HashMap<>();
            item.put("year",performance.getYear());
            item.put("quarter",performance.getQuarter());
            item.put("score",performance.getScore());
            result.add(item);
        }
        return result;
    }

    @Override
    public BigDecimal getDepartmentAvgScore(Integer year, Integer quarter, String department) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getDepartment,department);
        List<Employee> employees = employeeMapper.selectList(queryWrapper);
        if(employees.isEmpty()) return BigDecimal.ZERO;

        //获得员工工号
        List<String> empNos = employees.stream()
                .map(Employee::getEmpNo)
                .toList();
        LambdaQueryWrapper<Performance> perfWrapper = new LambdaQueryWrapper<>();
        perfWrapper.in(Performance::getEmpNo,empNos)
                .eq(Performance::getYear,year)
                .eq(Performance::getQuarter,quarter);
        List<Performance> performances = performanceMapper.selectList(perfWrapper);
        if(performances.isEmpty()) return BigDecimal.ZERO;
        //获得平均分
        BigDecimal avgScore = performances.stream()
                .map(Performance::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return avgScore.divide(new BigDecimal(performances.size()),2,RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getCompanyAvgScore(Integer year, Integer quarter) {
        return null;
    }
}
