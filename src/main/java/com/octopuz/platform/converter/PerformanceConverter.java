package com.octopuz.platform.converter;

import com.octopuz.platform.entity.Performance;
import com.octopuz.platform.vo.PerformanceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PerformanceConverter {
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    // VO → Entity
    Performance toEntity(PerformanceVO vo);
    // Entity → VO
    PerformanceVO toVO(Performance entity);
    // List 转换
    List<PerformanceVO> toVOList(List<Performance> entities);
}
