package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "channel_subscription")
@IdClass(ChannelSubscription.ChannelSubscriptionId.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChannelSubscription {

    @Id
    @Column(name = "subscriber_user_id")
    private Long subscriberUserId;

    @Id
    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "subscribed_at", insertable = false, updatable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "notifications_on")
    private Boolean notificationsOn = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_user_id", insertable = false, updatable = false)
    private User subscriber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", insertable = false, updatable = false)
    private Channel channel;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ChannelSubscriptionId implements Serializable {
        private Long subscriberUserId;
        private Long channelId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ChannelSubscriptionId that)) return false;
            return Objects.equals(subscriberUserId, that.subscriberUserId) &&
                   Objects.equals(channelId, that.channelId);
        }
        @Override
        public int hashCode() { return Objects.hash(subscriberUserId, channelId); }
    }
}
