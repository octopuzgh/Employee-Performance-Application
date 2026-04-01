package com.octopuz.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.enums.TrendType;
import com.octopuz.platform.mapper.AnalysisMapper;
import com.octopuz.platform.mapper.EmployeeMapper;
import com.octopuz.platform.mapper.PerformanceMapper;
import com.octopuz.platform.service.interf.AnalysisService;
import com.octopuz.platform.vo.DepartmentRankVO;
import com.octopuz.platform.vo.EmployeeTrendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class AnalysisServiceImpl extends ServiceImpl<PerformanceMapper, Performance> implements AnalysisService {
    @Autowired
    private PerformanceMapper performanceMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private AnalysisMapper analysisMapper;

//    @Override

//    public List<Map<String, Object>> getDepartmentRank(@Param("year") Integer year, @Param("quarter")Integer quarter) {
//        //查询季度所有绩效
//        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Performance::getYear,year)
//                    .eq(Performance::getQuarter,quarter);
//        List<Performance> performances = performanceMapper.selectList(queryWrapper);
//        //查询所有员工
//        List<Employee> employees = employeeMapper.selectList(null);
//        Map<String, Employee> employeeMap = employees.stream().collect(Collectors.toMap(Employee::getEmpNo, e -> e));
//        //按部门分组算平均分
//        //部门绩效
//        Map<String, List<Performance>> departmentPerformance = performances.stream()
//                .filter(p -> employeeMap.containsKey(p.getEmpNo()))
//                .collect(Collectors.groupingBy(p -> employeeMap.get(p.getEmpNo()).getDepartment()));
//        //部门平均分排名
//        List<Map<String, Object>> result = new ArrayList<>();
//        for (Map.Entry<String, List<Performance>> entry : departmentPerformance.entrySet()){
//            Map<String, Object> departmentPerformanceMap = new HashMap<>();
//            String key = entry.getKey();
//            List<Performance> value = entry.getValue();
//            BigDecimal avgScore = value.stream()
//                    .map(Performance::getScore)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add)
//                    .divide(new BigDecimal(value.size()), 2, RoundingMode.HALF_UP);
//            departmentPerformanceMap.put("department",key);
//            departmentPerformanceMap.put("avgScore",avgScore);
//            departmentPerformanceMap.put("count",value.size());
//            result.add(departmentPerformanceMap);
//        }
//        result.sort((o1, o2) -> ((BigDecimal)o2.get("avgScore")).compareTo((BigDecimal)o1.get("avgScore")));
//        int rank = 1;
//        for (Map<String, Object> item : result){
//            item.put("rank",rank);
//            rank++;
//        }
//        return result;
//    }

//    @Override
//    public List<Map<String, Object>> getEmployeeTrend(String empNo) {
//        //按工号获得绩效
//        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Performance::getEmpNo,empNo)
//                .orderByAsc(Performance::getYear,Performance::getQuarter);
//        List<Performance> performances = performanceMapper.selectList(queryWrapper);
//        ArrayList<Map<String, Object>> result = new ArrayList<>();
//        for (Performance performance : performances){
//            HashMap<String, Object> item = new HashMap<>();
//            item.put("year",performance.getYear());
//            item.put("quarter",performance.getQuarter());
//            item.put("score",performance.getScore());
//            result.add(item);
//        }
//        return result;
//    }

    @Override
    public BigDecimal getDepartmentAvgScore(Integer year, Integer quarter, String department) {
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getDepartment, department);
        List<Employee> employees = employeeMapper.selectList(queryWrapper);
        if (employees.isEmpty()) return BigDecimal.ZERO;

        //获得员工工号
        List<String> empNos = employees.stream()
                .map(Employee::getEmpNo)
                .toList();
        LambdaQueryWrapper<Performance> perfWrapper = new LambdaQueryWrapper<>();
        perfWrapper.in(Performance::getEmpNo, empNos)
                .eq(Performance::getYear, year)
                .eq(Performance::getQuarter, quarter);
        List<Performance> performances = performanceMapper.selectList(perfWrapper);
        if (performances.isEmpty()) return BigDecimal.ZERO;
        //获得平均分
        BigDecimal avgScore = performances.stream()
                .map(Performance::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return avgScore.divide(new BigDecimal(performances.size()), 2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal getCompanyAvgScore(Integer year, Integer quarter) {

        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Performance::getYear, year)
                .eq(Performance::getQuarter, quarter);
        return performanceMapper.selectList(queryWrapper).stream()
                .map(Performance::getScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(performanceMapper.selectList(queryWrapper).size()), 2, RoundingMode.HALF_UP);
    }

    //可不用
    @Override
    public List<DepartmentRankVO> getDepartmentRankVO(Integer year, Integer quarter) {
        return analysisMapper.getDepartmentRankVO(year, quarter);
    }

    //        return getDepartmentRank(year,quarter).stream()
//                .map(item -> DepartmentRankVO.builder()
//                        .department((String) item.get("department"))
//                        .avgScore((BigDecimal) item.get("avgScore"))
//                        .employeeCount((Integer) item.get("count"))
//                        .rank(item.get("rank") == null ? 0 : (Integer) item.get("rank"))
//                        .build())
//                .toList();
//    }
    //可不用
    @Override
    public List<EmployeeTrendVO> getEmployeeTrendVO(String empNo) {
        List<EmployeeTrendVO> employeeTrendVO = analysisMapper.getEmployeeTrendVO(empNo);
        //使用枚举

        for (EmployeeTrendVO et : employeeTrendVO) {
            if (et.getGrowthRate() == null) {
                et.setTrend(TrendType.FIRST);
            } else if (et.getGrowthRate().compareTo(BigDecimal.ZERO) > 0) {
                et.setTrend(TrendType.UP);
            } else if (et.getGrowthRate().compareTo(BigDecimal.ZERO) < 0) {
                et.setTrend(TrendType.DOWN);
            } else {
                et.setTrend(TrendType.STABLE);
            }
        }
        return employeeTrendVO;
    }
//
//        return getEmployeeTrend(empNo).stream()
//                .map(item -> EmployeeTrendVO.builder()
//                        .year((Integer) item.get("year"))
//                        .quarter((Integer) item.get("quarter"))
//                        .score((BigDecimal) item.get("score"))
//                        .build())
//                .toList();
//    }
}
