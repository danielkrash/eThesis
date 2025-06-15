package com.uni.ethesis.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.uni.ethesis.data.dto.ReviewDto;
import com.uni.ethesis.data.entities.Review;

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
}
