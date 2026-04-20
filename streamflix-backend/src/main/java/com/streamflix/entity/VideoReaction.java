package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "video_reaction")
@IdClass(VideoReaction.VideoReactionId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoReaction {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "video_id")
    private Long videoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Reaction reaction;

    @Column(name = "reacted_at", insertable = false, updatable = false)
    private LocalDateTime reactedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", insertable = false, updatable = false)
    private Video video;

    public enum Reaction {
        LIKE, DISLIKE
    }

    /** Composite primary key */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VideoReactionId implements Serializable {
        private Long userId;
        private Long videoId;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof VideoReactionId that))
                return false;
            return Objects.equals(userId, that.userId) &&
                    Objects.equals(videoId, that.videoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, videoId);
        }
    }
}
