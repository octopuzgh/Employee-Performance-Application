package com.octopuz.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopuz.platform.entity.Performance;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnalysisMapper extends BaseMapper<Performance> {
    //已经在pyspark 中实现
//    @Select("""
//                select
//                    e.department,
//                    avg(p.score) as avgScore,
//                    count(distinct e.emp_no) as employeeCount,
//                    rank() over (order by avg(p.score) desc) as departRank
//                from performance p
//                join employee e on p.emp_no = e.emp_no
//                where p.year = #{year} and p.quarter = #{quarter}
//                group by e.department
//                order by avgScore desc
//            """)
//    List<DepartmentRankVO> getDepartmentRankVO(
//            @Param("year") Integer year,
//            @Param("quarter") Integer quarter);
//
//    @Select("""
//                select
//                    p.emp_no,
//                    e.name,
//                    p.year,
//                    p.quarter,
//                    p.score,
//                    ROUND(
//                        (p.score - LAG(p.score, 1) OVER (ORDER BY p.year, p.quarter))/ LAG(p.score, 1) OVER (ORDER BY p.year, p.quarter) * 100,
//                        2
//                        ) as growthRate
//                from performance p
//                join employee e on p.emp_no = e.emp_no
//                where p.emp_no = #{empNo}
//                order by p.year, p.quarter
//            """)
//    List<EmployeeTrendVO> getEmployeeTrendVO(
//            @Param("empNo") String empNo);


}
