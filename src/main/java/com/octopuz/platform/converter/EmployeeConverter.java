package com.octopuz.platform.converter;

import com.octopuz.platform.entity.Employee;
import com.octopuz.platform.vo.EmployeeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EmployeeConverter {

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    // VO → Entity
    Employee toEntity(EmployeeVO vo);

    // Entity → VO
    EmployeeVO toVO(Employee entity);

    // List 转换
    List<EmployeeVO> toVOList(List<Employee> entities);
}
