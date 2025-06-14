package com.uni.ethesis.utils.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.DepartmentAppointmentDto;
import com.uni.ethesis.data.entities.DepartmentAppointment;

@Mapper(componentModel = "spring")
public interface DepartmentAppointmentMapper {

    DepartmentAppointmentMapper INSTANCE = Mappers.getMapper(DepartmentAppointmentMapper.class);

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "department.id", target = "departmentId")
    @Mapping(source = "user", target = "userFullName", qualifiedByName = "mapUserFullName")
    @Mapping(source = "user.email", target = "userEmail")
    @Mapping(source = "department.name", target = "departmentName")
    @Mapping(source = "startDate", target = "startDate", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    @Mapping(source = "endDate", target = "endDate", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    @Mapping(source = "lastModifiedAt", target = "lastModifiedAt", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    @Mapping(target = "isCurrent", ignore = true) // Will be set manually in service
    DepartmentAppointmentDto departmentAppointmentToDto(DepartmentAppointment departmentAppointment);

    @Mapping(target = "user", ignore = true) // Will be set manually in service
    @Mapping(target = "department", ignore = true) // Will be set manually in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    @Mapping(source = "startDate", target = "startDate", qualifiedByName = "mapLocalDateTimeToOffsetDateTime")
    @Mapping(source = "endDate", target = "endDate", qualifiedByName = "mapLocalDateTimeToOffsetDateTime")
    DepartmentAppointment dtoToDepartmentAppointment(DepartmentAppointmentDto dto);

    List<DepartmentAppointmentDto> departmentAppointmentsToDto(List<DepartmentAppointment> departmentAppointments);

    @Named("mapUserFullName")
    default String mapUserFullName(com.uni.ethesis.data.entities.User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }

    @Named("mapOffsetDateTimeToLocalDateTime")
    default java.time.LocalDateTime mapOffsetDateTimeToLocalDateTime(java.time.OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }

    @Named("mapLocalDateTimeToOffsetDateTime")
    default java.time.OffsetDateTime mapLocalDateTimeToOffsetDateTime(java.time.LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.atOffset(java.time.ZoneOffset.UTC) : null;
    }
}
