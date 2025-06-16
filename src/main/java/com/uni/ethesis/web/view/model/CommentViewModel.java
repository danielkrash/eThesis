package com.uni.ethesis.web.view.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentViewModel {
    private String id;
    private String content;
    private String reviewId;
    private String userId;
    private String commenterName;
    private String createdAt;
    private String updatedAt;
}
