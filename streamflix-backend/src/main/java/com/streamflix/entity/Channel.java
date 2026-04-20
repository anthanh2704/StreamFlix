package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "channel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "channel_id")
    private Long channelId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", unique = true)
    private User owner;

    @Column(name = "channel_name", nullable = false, length = 100)
    private String channelName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "banner_url", length = 500)
    private String bannerUrl;

    @Column(name = "subscriber_count")
    private Long subscriberCount = 0L;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
