package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "video", indexes = {
        @Index(name = "idx_video_upload", columnList = "upload_date DESC"),
        @Index(name = "idx_video_views", columnList = "views_count DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Video {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "video_id")
    private Long videoId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "video_url", nullable = false, length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "duration_sec", nullable = false)
    private Integer durationSec;

    @Column(length = 10)
    private String resolution = "HD";

    @Column(name = "views_count")
    private Long viewsCount = 0L;

    @Column(name = "likes_count")
    private Long likesCount = 0L;

    @Column(name = "dislikes_count")
    private Long dislikesCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status = Status.PUBLISHED;

    @Column(name = "is_premium")
    private Boolean isPremium = false;

    @Column(name = "upload_date", insertable = false, updatable = false)
    private LocalDateTime uploadDate;

    // M:N categories
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "video_category", joinColumns = @JoinColumn(name = "video_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    @Builder.Default
    private Set<Category> categories = new HashSet<>();

    // M:N tags
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "video_tag", joinColumns = @JoinColumn(name = "video_id"), inverseJoinColumns = @JoinColumn(name = "tag_id"))
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    public enum Status {
        DRAFT, PUBLISHED, PRIVATE, REMOVED
    }
}
