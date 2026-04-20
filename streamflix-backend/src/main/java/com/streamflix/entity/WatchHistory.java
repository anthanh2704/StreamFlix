package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "watch_history", indexes = @Index(name = "idx_history_user_time", columnList = "user_id, watched_at DESC"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id")
    private Video video;

    @Column(name = "watched_at", insertable = false, updatable = false)
    private LocalDateTime watchedAt;

    @Column(name = "watch_duration", nullable = false)
    private Integer watchDuration = 0;

    @Column(name = "progress_pct", precision = 5, scale = 2)
    private BigDecimal progressPct = BigDecimal.ZERO;

    @Column(name = "device_type", length = 30)
    private String deviceType;
}
