package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.CommentDto;
import com.streamflix.service.CommentService;
import com.streamflix.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    /** Top-level comments for a video (public). */
    @GetMapping("/video/{videoId}")
    public ApiResponse<Page<CommentDto.Response>> list(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(page, size);
        return ApiResponse.ok(commentService.topLevel(videoId, p).map(CommentDto.Response::fromEntity));
    }

    /** Replies to a specific comment. */
    @GetMapping("/{commentId}/replies")
    public ApiResponse<List<CommentDto.Response>> replies(@PathVariable Long commentId) {
        return ApiResponse.ok(commentService.replies(commentId)
                .stream().map(CommentDto.Response::fromEntity).toList());
    }

    /** Post a comment (or a reply when parentCommentId is set). */
    @PostMapping("/video/{videoId}")
    public ApiResponse<CommentDto.Response> post(
            @PathVariable Long videoId,
            @Valid @RequestBody CommentDto.Request req,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok("Comment posted",
                CommentDto.Response.fromEntity(commentService.post(userId, videoId, req)));
    }

    @DeleteMapping("/{commentId}")
    public ApiResponse<Object> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        return ApiResponse.ok("Comment deleted", null);
    }
}
