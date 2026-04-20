package com.streamflix.repository;

import com.streamflix.entity.VideoReaction;
import com.streamflix.entity.VideoReaction.VideoReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoReactionRepository extends JpaRepository<VideoReaction, VideoReactionId> {
    Optional<VideoReaction> findByUserIdAndVideoId(Long userId, Long videoId);
}
