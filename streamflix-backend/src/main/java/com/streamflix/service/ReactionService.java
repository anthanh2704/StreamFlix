package com.streamflix.service;

import com.streamflix.entity.VideoReaction;
import com.streamflix.repository.VideoReactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReactionService {

    private final VideoReactionRepository reactionRepository;
    private final UserService userService;
    private final VideoService videoService;

    /*
     * Toggle a user's reaction on a video.
     * - If no reaction yet → insert (trigger auto-increments video counters)
     * - If same reaction → remove (trigger auto-decrements)
     * - If opposite reaction → update reaction column (counters adjusted manually)
     */
    public String react(Long userId, Long videoId, VideoReaction.Reaction target) {
        userService.findById(userId);
        videoService.findById(videoId);

        return reactionRepository.findByUserIdAndVideoId(userId, videoId)
                .map(existing -> {
                    if (existing.getReaction() == target) {
                        reactionRepository.delete(existing);
                        return "REMOVED";
                    } else {
                        existing.setReaction(target);
                        reactionRepository.save(existing);
                        return "SWITCHED";
                    }
                })
                .orElseGet(() -> {
                    reactionRepository.save(VideoReaction.builder()
                            .userId(userId).videoId(videoId).reaction(target).build());
                    return "ADDED";
                });
    }
}
