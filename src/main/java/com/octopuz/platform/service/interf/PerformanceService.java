package com.octopuz.platform.service.interf;

import com.baomidou.mybatisplus.extension.service.IService;
import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.vo.PerformanceVO;

import java.util.List;

public interface PerformanceService extends IService<Performance> {
    //按工号查绩效
    List<Performance> getByEmpNo(String empNo);
    //按年份查绩效
    List<Performance> getByYear(Integer year);
    //按年份和季度查绩效
    List<Performance> getByYearAndQuarter(Integer year, Integer quarter);
    //添加或修改绩效
    boolean addOrUpdate(Performance performance);
    //转换为VO
    PerformanceVO convertToVO(Performance performance);
    List<PerformanceVO> convertToVOList(List<Performance> performances);

}
