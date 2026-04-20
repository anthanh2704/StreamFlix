package com.streamflix.dto;

import com.streamflix.entity.Video;

import java.time.LocalDateTime;
import java.util.List;

public record VideoResponse(
                Long videoId,
                Long channelId,
                String channelName,
                String title,
                String description,
                String videoUrl,
                String thumbnailUrl,
                Integer durationSec,
                String resolution,
                Long viewsCount,
                Long likesCount,
                Long dislikesCount,
                String status,
                Boolean isPremium,
                LocalDateTime uploadDate,
                List<String> categories,
                List<String> tags) {
        public static VideoResponse fromEntity(Video v) {
                return new VideoResponse(
                                v.getVideoId(),
                                v.getChannel() != null ? v.getChannel().getChannelId() : null,
                                v.getChannel() != null ? v.getChannel().getChannelName() : null,
                                v.getTitle(),
                                v.getDescription(),
                                v.getVideoUrl(),
                                v.getThumbnailUrl(),
                                v.getDurationSec(),
                                v.getResolution(),
                                v.getViewsCount(),
                                v.getLikesCount(),
                                v.getDislikesCount(),
                                v.getStatus() != null ? v.getStatus().name() : null,
                                v.getIsPremium(),
                                v.getUploadDate(),
                                v.getCategories() == null ? List.of()
                                                : v.getCategories().stream().map(c -> c.getName()).toList(),
                                v.getTags() == null ? List.of()
                                                : v.getTags().stream().map(t -> t.getName()).toList());
        }
}
