package com.streamflix.controller;

import com.streamflix.dto.*;
import com.streamflix.entity.Video;
import com.streamflix.entity.VideoReaction;
import com.streamflix.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final WatchService watchService;
    private final ReactionService reactionService;
    private final UserService userService;

    // Browsing & search (public)

    @GetMapping
    public ApiResponse<Page<VideoResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadDate"));
        return ApiResponse.ok(videoService.listPublished(p).map(VideoResponse::fromEntity));
    }

    @GetMapping("/search")
    public ApiResponse<Page<VideoResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(page, size);
        return ApiResponse.ok(videoService.search(q, p).map(VideoResponse::fromEntity));
    }

    @GetMapping("/trending")
    public ApiResponse<List<VideoResponse>> trending(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(videoService.trending(days, limit)
                .stream().map(VideoResponse::fromEntity).toList());
    }

    @GetMapping("/category/{categoryId}")
    public ApiResponse<Page<VideoResponse>> byCategory(
            @PathVariable Integer categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable p = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "uploadDate"));
        return ApiResponse.ok(videoService.byCategory(categoryId, p).map(VideoResponse::fromEntity));
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoResponse> get(@PathVariable Long videoId) {
        return ApiResponse.ok(VideoResponse.fromEntity(videoService.findById(videoId)));
    }

    // Creation (authenticated creator)

    @PostMapping("/channel/{channelId}")
    public ApiResponse<VideoResponse> upload(
            @PathVariable Long channelId,
            @Valid @RequestBody VideoCreateRequest req) {
        Video v = videoService.uploadVideo(channelId, req);
        return ApiResponse.ok("Video uploaded", VideoResponse.fromEntity(v));
    }

    @DeleteMapping("/{videoId}")
    public ApiResponse<Object> delete(@PathVariable Long videoId) {
        videoService.delete(videoId);
        return ApiResponse.ok("Video removed", null);
    }

    // Interaction (authenticated)

    @PostMapping("/{videoId}/watch")
    public ApiResponse<Object> watch(
            @PathVariable Long videoId,
            @Valid @RequestBody WatchRequest req,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        watchService.record(userId, videoId, req);
        return ApiResponse.ok("Watch recorded", null);
    }

    @PostMapping("/{videoId}/like")
    public ApiResponse<String> like(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(reactionService.react(userId, videoId, VideoReaction.Reaction.LIKE));
    }

    @PostMapping("/{videoId}/dislike")
    public ApiResponse<String> dislike(
            @PathVariable Long videoId,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(reactionService.react(userId, videoId, VideoReaction.Reaction.DISLIKE));
    }

    // Recommendations

    @GetMapping("/recommendations")
    public ApiResponse<List<VideoResponse>> recommendations(
            @RequestParam(defaultValue = "10") int limit,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(videoService.recommendations(userId, limit)
                .stream().map(VideoResponse::fromEntity).toList());
    }
}
