package com.streamflix.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "playlist_video")
@IdClass(PlaylistVideo.PlaylistVideoId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaylistVideo {

    @Id
    @Column(name = "playlist_id")
    private Long playlistId;

    @Id
    @Column(name = "video_id")
    private Long videoId;

    @Column(nullable = false)
    private Integer position;

    @Column(name = "added_at", insertable = false, updatable = false)
    private LocalDateTime addedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id", insertable = false, updatable = false)
    private Playlist playlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", insertable = false, updatable = false)
    private Video video;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PlaylistVideoId implements Serializable {
        private Long playlistId;
        private Long videoId;

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PlaylistVideoId that))
                return false;
            return Objects.equals(playlistId, that.playlistId) &&
                    Objects.equals(videoId, that.videoId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(playlistId, videoId);
        }
    }
}
