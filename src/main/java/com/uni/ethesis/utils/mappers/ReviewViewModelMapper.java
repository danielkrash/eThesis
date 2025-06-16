package com.uni.ethesis.utils.mappers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.CommentDto;
import com.uni.ethesis.data.dto.ReviewDto;
import com.uni.ethesis.web.view.model.CommentViewModel;
import com.uni.ethesis.web.view.model.ThesisReviewViewModel;

@Mapper(componentModel = "spring")
public interface ReviewViewModelMapper {

    ReviewViewModelMapper INSTANCE = Mappers.getMapper(ReviewViewModelMapper.class);

    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "teacherId", target = "teacherId", qualifiedByName = "uuidToString")
    @Mapping(source = "thesisId", target = "thesisId", qualifiedByName = "uuidToString")
    @Mapping(source = "conclusion", target = "conclusion", qualifiedByName = "conclusionToString")
    @Mapping(target = "comments", ignore = true) // Will be set manually
    ThesisReviewViewModel reviewDtoToThesisReviewViewModel(ReviewDto reviewDto);

    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "userId", target = "userId", qualifiedByName = "uuidToString")
    @Mapping(source = "reviewId", target = "reviewId", qualifiedByName = "uuidToString")
    CommentViewModel commentDtoToCommentViewModel(CommentDto commentDto);

    default Set<CommentViewModel> commentDtosToCommentViewModels(List<CommentDto> commentDtos) {
        if (commentDtos == null) {
            return null;
        }
        return commentDtos.stream()
                .map(this::commentDtoToCommentViewModel)
                .collect(Collectors.toSet());
    }

    @Named("uuidToString")
    default String uuidToString(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("conclusionToString")
    default String conclusionToString(com.uni.ethesis.enums.ReviewConclusion conclusion) {
        return conclusion != null ? conclusion.toString() : null;
    }
}
