package com.uni.ethesis.web.view.model;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ThesisReviewViewModel {
    private String id;
    private String content;
    private String conclusion;
    private String teacherId;
    private String thesisId;
    private String reviewerName;
    private String createdAt;
    private String updatedAt;
    private List<CommentViewModel> comments;
}
