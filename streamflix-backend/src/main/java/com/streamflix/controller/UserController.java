package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.dto.UserResponse;
import com.streamflix.dto.VideoResponse;
import com.streamflix.entity.User;
import com.streamflix.entity.WatchHistory;
import com.streamflix.service.UserService;
import com.streamflix.service.WatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final WatchService watchService;

    public record HistoryItem(Long historyId, VideoResponse video,
            Integer watchDuration, LocalDateTime watchedAt, String deviceType) {
        public static HistoryItem from(WatchHistory h) {
            return new HistoryItem(
                    h.getHistoryId(),
                    VideoResponse.fromEntity(h.getVideo()),
                    h.getWatchDuration(),
                    h.getWatchedAt(),
                    h.getDeviceType());
        }
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> get(@PathVariable Long id) {
        User u = userService.findById(id);
        return ApiResponse.ok(UserResponse.fromEntity(u));
    }

    @GetMapping("/me/history")
    public ApiResponse<Page<HistoryItem>> myHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        Pageable p = PageRequest.of(page, size);
        return ApiResponse.ok(watchService.history(userId, p).map(HistoryItem::from));
    }
}
