package com.octopuz.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.octopuz.platform.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

}
