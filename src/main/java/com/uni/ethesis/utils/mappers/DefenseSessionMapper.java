package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.DefenseSessionDto;
import com.uni.ethesis.data.entities.DefenseSession;

@Mapper(componentModel = "spring")
public interface DefenseSessionMapper {

    DefenseSessionMapper INSTANCE = Mappers.getMapper(DefenseSessionMapper.class);

    @Mapping(source = "thesis.id", target = "thesisId")
    @Mapping(source = "defense.id", target = "defenseId")
    DefenseSessionDto defenseSessionToDefenseSessionDto(DefenseSession defenseSession);

    @Mapping(target = "thesis", ignore = true) // Will be set manually in service
    @Mapping(target = "defense", ignore = true) // Will be set manually in service
    @Mapping(target = "professors", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    DefenseSession defenseSessionDtoToDefenseSession(DefenseSessionDto defenseSessionDto);
}
