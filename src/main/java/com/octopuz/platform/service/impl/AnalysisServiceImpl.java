package com.octopuz.platform.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.enums.TrendType;
import com.octopuz.platform.mapper.AnalysisMapper;
import com.octopuz.platform.mapper.EmployeeMapper;
import com.octopuz.platform.mapper.PerformanceMapper;
import com.octopuz.platform.service.interf.AnalysisService;
import com.octopuz.platform.utils.PythonScriptExecutor;
import com.octopuz.platform.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
@Slf4j
@Service
public class AnalysisServiceImpl extends ServiceImpl<PerformanceMapper, Performance> implements AnalysisService {
    @Autowired
    private PerformanceMapper performanceMapper;
    @Autowired
    private EmployeeMapper employeeMapper;
    @Autowired
    private AnalysisMapper analysisMapper;
    @Autowired
    private PythonScriptExecutor pythonScriptExecutor;

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
    @Cacheable(value = "analysis:dept-avg",
                key = "#year+'-'+#quarter")
    @Override
    public DepartmentAvgScoreVO getDepartmentAvgScore(Integer year, Integer quarter, String department) {
        try {
            String jsonResult = pythonScriptExecutor.execute("dept_avg",
                    String.valueOf(year), String.valueOf(quarter), department);

            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);

            List<DepartmentAvgScoreVO> list = JSON.parseArray(cleanedJson, DepartmentAvgScoreVO.class);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.error("部门平均分查询失败", e);
            throw new RuntimeException("部门平均分查询失败: " + e.getMessage(), e);
        }
    }
        //        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Employee::getDepartment, department);
//        List<Employee> employees = employeeMapper.selectList(queryWrapper);
//        if (employees.isEmpty()) return BigDecimal.ZERO;
//
//        //获得员工工号
//        List<String> empNos = employees.stream()
//                .map(Employee::getEmpNo)
//                .toList();
//        LambdaQueryWrapper<Performance> perfWrapper = new LambdaQueryWrapper<>();
//        perfWrapper.in(Performance::getEmpNo, empNos)
//                .eq(Performance::getYear, year)
//                .eq(Performance::getQuarter, quarter);
//        List<Performance> performances = performanceMapper.selectList(perfWrapper);
//        if (performances.isEmpty()) return BigDecimal.ZERO;
//        //获得平均分
//        BigDecimal avgScore = performances.stream()
//                .map(Performance::getScore)
//                .reduce(BigDecimal.ZERO, BigDecimal::add);
//        return avgScore.divide(new BigDecimal(performances.size()), 2, RoundingMode.HALF_UP);
//    }

    @Cacheable(value = "analysis:company-avg",
                key = "#year+'-'+#quarter")
    @Override
    public CompanyAvgScoreVO getCompanyAvgScore(Integer year, Integer quarter) {
        try {
            String jsonResult = pythonScriptExecutor.execute("company_avg",
                    String.valueOf(year), String.valueOf(quarter));

            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);

            List<CompanyAvgScoreVO> list = JSON.parseArray(cleanedJson, CompanyAvgScoreVO.class);
            return list.isEmpty() ? null : list.get(0);
        } catch (Exception e) {
            log.error("公司平均分查询失败", e);
            throw new RuntimeException("公司平均分查询失败: " + e.getMessage(), e);
        }
    }
//        LambdaQueryWrapper<Performance> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Performance::getYear, year)
//                .eq(Performance::getQuarter, quarter);
//        return performanceMapper.selectList(queryWrapper).stream()
//                .map(Performance::getScore)
//                .reduce(BigDecimal.ZERO, BigDecimal::add)
//                .divide(new BigDecimal(performanceMapper.selectList(queryWrapper).size()), 2, RoundingMode.HALF_UP);
//    }


    @Cacheable(value = "analysis:rank",
            key = "#year+'-'+#quarter")
    @Override
    public List<DepartmentRankVO> getDepartmentRankVO(Integer year, Integer quarter) {
        try {
            String jsonResult = pythonScriptExecutor.execute("dept_rank",
                    String.valueOf(year), String.valueOf(quarter));

            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);

            return JSON.parseArray(cleanedJson, DepartmentRankVO.class);
        } catch (Exception e) {
            log.error("部门排名查询失败", e);
            throw new RuntimeException("部门排名查询失败: " + e.getMessage(), e);
        }
        //return analysisMapper.getDepartmentRankVO(year, quarter);
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


    @Cacheable(value = "analysis:trend",
            key = "#empNo")
    @Override
    public List<EmployeeTrendVO> getEmployeeTrendVO(String empNo) {
        try {
            String jsonResult = pythonScriptExecutor.execute("emp_trend", empNo);

            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);

            List<EmployeeTrendVO> trends = JSON.parseArray(cleanedJson, EmployeeTrendVO.class);

            // 计算 trend 字段
            for (EmployeeTrendVO et : trends) {
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

            return trends;
        } catch (Exception e) {
            log.error("员工趋势查询失败", e);
            throw new RuntimeException("员工趋势查询失败: " + e.getMessage(), e);
        }
        //return analysisMapper.getEmployeeTrendVO(empNo);
    }

    @Override
    @Cacheable(value = "analysis:dept-stats")
    public List<DepartmentStatsVO> getDepartmentStats() {
        try {
            log.info("开始调用 Python 脚本: dept_stats");
            String jsonResult = pythonScriptExecutor.execute("dept_stats");
            log.info("Python 脚本返回长度: {}", jsonResult.length());

            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);
            log.info("清理后 JSON: {}", cleanedJson.substring(0, Math.min(100, cleanedJson.length())));

            List<DepartmentStatsVO> result = JSON.parseArray(cleanedJson, DepartmentStatsVO.class);
            log.info("解析成功，数据条数: {}", result.size());

            return result;
        } catch (Exception e) {
            log.error("dept_stats.py脚本执行错误", e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "analysis:emp-rank")
    public List<EmployeeRankVO> getEmployeeRank(Integer topN) {
        try {
            if(topN == null || topN <= 0){
                topN = 10;
            }
            String jsonResult = pythonScriptExecutor.execute("emp_rank", String.valueOf(topN));
            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);

            return JSON.parseArray(cleanedJson, EmployeeRankVO.class);
        } catch (Exception e) {
            log.error("Python脚本执行错误", e);
            return null;
        }
    }

    @Override
    @Cacheable(value = "analysis:company-summary", key = "(#year ?: 'all') + '-' + (#quarter ?: 'all')")
    public CompanySummaryVO getCompanySummary(Integer year,Integer quarter) {
        try {
            List<String> args = new ArrayList<>();
            if(year != null){
                args.add(String.valueOf(year));
                if (quarter != null) {
                    args.add(String.valueOf(quarter));
                }
            }
            String jsonResult = pythonScriptExecutor.execute("company_summary", args.toArray(new String[0]));
            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);
            return JSON.parseObject(cleanedJson, CompanySummaryVO.class);
        } catch (Exception e) {
            log.error("公司总览查询失败", e);
            throw new RuntimeException("公司总览查询失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Cacheable(value = "analysis:anomaly-detect", key = "#threshold ?: '20'")
    public List<AnomalyDetectVO> getAnomalyDetect(BigDecimal threshold) {
        try {
            if (threshold == null) {
                threshold = new BigDecimal("20");
            }

            String jsonResult = pythonScriptExecutor.execute("anomaly_detect",
                    threshold.toString());

            String cleanedJson = pythonScriptExecutor.extractJson(jsonResult);

            return JSON.parseArray(cleanedJson, AnomalyDetectVO.class);
        } catch (Exception e) {
            log.error("异常检测查询失败", e);
            throw new RuntimeException("异常检测查询失败: " + e.getMessage(), e);
        }
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
