package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.DefenseDto;
import com.uni.ethesis.data.entities.Defense;

@Mapper(componentModel = "spring")
public interface DefenseMapper {

    DefenseMapper INSTANCE = Mappers.getMapper(DefenseMapper.class);

    DefenseDto defenseToDefenseDto(Defense defense);

    @Mapping(target = "defenses", ignore = true)
    @Mapping(target = "defenseSessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Defense defenseDtoToDefense(DefenseDto defenseDto);
}
