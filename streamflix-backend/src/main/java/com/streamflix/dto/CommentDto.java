package com.streamflix.dto;

import com.streamflix.entity.Comment;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class CommentDto {

    public record Request(
            @NotBlank String content,
            Long parentCommentId) {
    }

    public record Response(
            Long commentId,
            Long videoId,
            Long userId,
            String username,
            Long parentCommentId,
            String content,
            Integer likesCount,
            LocalDateTime createdAt) {
        public static Response fromEntity(Comment c) {
            return new Response(
                    c.getCommentId(),
                    c.getVideo() != null ? c.getVideo().getVideoId() : null,
                    c.getUser() != null ? c.getUser().getUserId() : null,
                    c.getUser() != null ? c.getUser().getUsername() : null,
                    c.getParent() != null ? c.getParent().getCommentId() : null,
                    c.getContent(),
                    c.getLikesCount(),
                    c.getCreatedAt());
        }
    }
}
