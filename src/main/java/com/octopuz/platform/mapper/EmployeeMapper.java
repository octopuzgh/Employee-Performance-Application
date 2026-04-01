package com.octopuz.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.vo.DepartmentRankVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
