package com.uni.ethesis.utils.mappers;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.ThesisDto;
import com.uni.ethesis.data.entities.Thesis;
import com.uni.ethesis.enums.ThesisStatus;
import com.uni.ethesis.web.view.model.ThesisViewModel;

@Mapper(componentModel = "spring")
public interface ThesisMapper {

    ThesisMapper INSTANCE = Mappers.getMapper(ThesisMapper.class);

    // ============================================
    // Entity ↔ DTO Mappings
    // ============================================

    @Mapping(source = "proposal.id", target = "proposalId")
    ThesisDto thesisToThesisDto(Thesis thesis);

    @Mapping(target = "proposal", ignore = true) // Will be set manually in service
    @Mapping(target = "sessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Thesis thesisDtoToThesis(ThesisDto thesisDto);

    // ============================================
    // DTO ↔ ViewModel Mappings
    // ============================================
    
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "status", target = "status", qualifiedByName = "thesisStatusToString")
    @Mapping(source = "pdfPath", target = "pdfPath") // Map pdfPath directly
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(source = "lastModifiedAt", target = "lastModifiedAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "title", ignore = true) // Will be set from proposal in service
    @Mapping(target = "goal", ignore = true) // Will be set from proposal in service
    @Mapping(target = "objectives", ignore = true) // Will be set from proposal in service
    @Mapping(target = "technology", ignore = true) // Will be set from proposal in service
    @Mapping(target = "studentId", ignore = true) // Will be set from proposal in service
    @Mapping(target = "teacherId", ignore = true) // Will be set from proposal in service
    @Mapping(target = "departmentId", ignore = true) // Will be set from proposal in service
    @Mapping(target = "studentName", ignore = true) // Will be set in service
    @Mapping(target = "teacherName", ignore = true) // Will be set in service
    @Mapping(target = "reviews", ignore = true) // Will be set in service
    ThesisViewModel thesisDtoToViewModel(ThesisDto thesisDto);

    @Mapping(source = "id", target = "id", qualifiedByName = "stringToUuid")
    @Mapping(source = "status", target = "status", qualifiedByName = "stringToThesisStatus")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "stringToOffsetDateTime")
    @Mapping(source = "lastModifiedAt", target = "lastModifiedAt", qualifiedByName = "stringToOffsetDateTime")
    @Mapping(target = "pdfPath", ignore = true)
    @Mapping(target = "finalGrade", ignore = true)
    @Mapping(target = "proposalId", ignore = true) // Will be set from studentId in service
    ThesisDto thesisViewModelToDto(ThesisViewModel thesisViewModel);

    // ============================================
    // Helper Methods
    // ============================================
    
    @Named("uuidToString")
    default String uuidToString(UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("stringToUuid")
    default UUID stringToUuid(String str) {
        return str != null && !str.isEmpty() ? UUID.fromString(str) : null;
    }

    @Named("thesisStatusToString")
    default String thesisStatusToString(ThesisStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("stringToThesisStatus")
    default ThesisStatus stringToThesisStatus(String status) {
        return status != null && !status.isEmpty() ? ThesisStatus.valueOf(status) : null;
    }

    @Named("offsetDateTimeToString")
    default String offsetDateTimeToString(OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }

    @Named("stringToOffsetDateTime")
    default OffsetDateTime stringToOffsetDateTime(String dateTimeStr) {
        return dateTimeStr != null && !dateTimeStr.isEmpty() ? 
            OffsetDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null;
    }
}
