package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.ReviewDto;
import com.uni.ethesis.data.entities.Review;
import com.uni.ethesis.web.view.model.ThesisReviewViewModel;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    ReviewMapper INSTANCE = Mappers.getMapper(ReviewMapper.class);

    @Mapping(source = "teacher.id", target = "teacherId")
    @Mapping(source = "thesis.id", target = "thesisId")
    ReviewDto reviewToReviewDto(Review review);

    @Mapping(target = "teacher", ignore = true) // Will be set manually in service
    @Mapping(target = "thesis", ignore = true) // Will be set manually in service
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastModifiedAt", ignore = true)
    Review reviewDtoToReview(ReviewDto reviewDto);

    // DTO to ViewModel mapping
    @Mapping(source = "id", target = "id", qualifiedByName = "uuidToString")
    @Mapping(source = "teacherId", target = "teacherId", qualifiedByName = "uuidToString")
    @Mapping(source = "thesisId", target = "thesisId", qualifiedByName = "uuidToString")
    @Mapping(source = "conclusion", target = "conclusion", qualifiedByName = "conclusionToString")
    @Mapping(source = "createdAt", target = "createdAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(source = "lastModifiedAt", target = "updatedAt", qualifiedByName = "offsetDateTimeToString")
    @Mapping(target = "comments", ignore = true) // Will be set in controller
    @Mapping(target = "reviewerName", ignore = true) // Will be set in controller
    ThesisReviewViewModel reviewDtoToThesisReviewViewModel(ReviewDto reviewDto);

    @Named("uuidToString")
    default String uuidToString(java.util.UUID uuid) {
        return uuid != null ? uuid.toString() : null;
    }

    @Named("conclusionToString")
    default String conclusionToString(com.uni.ethesis.enums.ReviewConclusion conclusion) {
        if (conclusion == null) {
            return null;
        }
        switch (conclusion) {
            case ACCEPTED:
                return "ACCEPTED";
            case REJECTED:
                return "REJECTED";
            default:
                return conclusion.name();
        }
    }

    @Named("offsetDateTimeToString")
    default String offsetDateTimeToString(java.time.OffsetDateTime dateTime) {
        return dateTime != null ? dateTime.toString() : null;
    }
}
