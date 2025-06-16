package com.uni.ethesis.utils.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.CommentDto;
import com.uni.ethesis.data.entities.Comment;
import com.uni.ethesis.web.view.model.CommentViewModel;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentMapper INSTANCE = Mappers.getMapper(CommentMapper.class);

    @Mapping(source = "review.id", target = "reviewId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user", target = "userFullName", qualifiedByName = "mapUserFullName")
    @Mapping(source = "user", target = "userRole", qualifiedByName = "mapUserRole")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    @Mapping(source = "lastModifiedAt", target = "updatedAt", qualifiedByName = "mapOffsetDateTimeToLocalDateTime")
    CommentDto commentToCommentDto(Comment comment);

    @Mapping(target = "review", ignore = true) // Will be set manually in service
    @Mapping(target = "user", ignore = true) // Will be set manually in service
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Comment commentDtoToComment(CommentDto commentDto);

    List<CommentDto> commentsToCommentDtos(List<Comment> comments);

    // DTO to ViewModel mapping
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "userId", target = "userId", qualifiedByName = "uuidToString")
    @Mapping(source = "reviewId", target = "reviewId", qualifiedByName = "uuidToString")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "localDateTimeToString")
    @Mapping(source = "updatedAt", target = "updatedAt", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "commenterName", ignore = true) // Will be set in controller
    CommentViewModel commentDtoToCommentViewModel(CommentDto commentDto);

    List<CommentViewModel> commentDtosToCommentViewModels(List<CommentDto> commentDtos);

    @Named("uuidToString")
    default String uuidToString(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("mapUserFullName")
    default String mapUserFullName(com.uni.ethesis.data.entities.User user) {
        if (user == null) {
            return null;
        }
        return user.getFirstName() + " " + user.getLastName();
    }

    @Named("mapUserRole")
    default String mapUserRole(com.uni.ethesis.data.entities.User user) {
        // This will need to be handled in the service layer with proper repository access
        // For now, we'll return a default value and handle it in the service
        return "STUDENT"; // Default, will be overridden in service
    }

    @Named("mapOffsetDateTimeToLocalDateTime")
    default java.time.LocalDateTime mapOffsetDateTimeToLocalDateTime(java.time.OffsetDateTime offsetDateTime) {
        return offsetDateTime != null ? offsetDateTime.toLocalDateTime() : null;
    }

    @Named("localDateTimeToString")
    default String localDateTimeToString(java.time.LocalDateTime localDateTime) {
        return localDateTime != null ? localDateTime.toString() : null;
    }
}
