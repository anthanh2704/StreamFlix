package com.streamflix.controller;

import com.streamflix.dto.ApiResponse;
import com.streamflix.entity.Channel;
import com.streamflix.service.ChannelService;
import com.streamflix.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final UserService    userService;

    public record ChannelView(Long channelId, String name, String description,
                               Long subscriberCount, String ownerUsername) {
        public static ChannelView fromEntity(Channel c) {
            return new ChannelView(c.getChannelId(), c.getChannelName(),
                    c.getDescription(), c.getSubscriberCount(),
                    c.getOwner() != null ? c.getOwner().getUsername() : null);
        }
    }

    @GetMapping
    public ApiResponse<List<ChannelView>> all() {
        return ApiResponse.ok(channelService.all().stream()
                .map(ChannelView::fromEntity).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ChannelView> get(@PathVariable Long id) {
        return ApiResponse.ok(ChannelView.fromEntity(channelService.findById(id)));
    }

    /** Create your own channel — must be authenticated. */
    @PostMapping
    public ApiResponse<ChannelView> create(
            @RequestBody Map<String,String> body,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        Channel ch = channelService.createChannel(userId,
                body.get("channelName"), body.get("description"));
        return ApiResponse.ok("Channel created", ChannelView.fromEntity(ch));
    }

    /** Subscribe / unsubscribe toggle. */
    @PostMapping("/{channelId}/subscribe")
    public ApiResponse<String> subscribe(
            @PathVariable Long channelId,
            @AuthenticationPrincipal UserDetails principal) {
        Long userId = userService.findByUsername(principal.getUsername()).getUserId();
        return ApiResponse.ok(channelService.subscribe(userId, channelId));
    }
}
