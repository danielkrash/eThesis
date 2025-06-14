package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.entities.Thesis;

@Mapper(componentModel = "spring")
public interface ThesisMapper {

    ThesisMapper INSTANCE = Mappers.getMapper(ThesisMapper.class);

    @Mapping(source = "proposal.id", target = "proposalId")
    ThesisDto thesisToThesisDto(Thesis thesis);

    @Mapping(target = "proposal", ignore = true) // Will be set manually in service
    @Mapping(target = "sessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Thesis thesisDtoToThesis(ThesisDto thesisDto);
}
