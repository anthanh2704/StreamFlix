package com.streamflix.service;

import com.streamflix.dto.WatchRequest;
import com.streamflix.entity.User;
import com.streamflix.entity.Video;
import com.streamflix.entity.WatchHistory;
import com.streamflix.repository.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class WatchService {

    private final WatchHistoryRepository historyRepository;
    private final UserService userService;
    private final VideoService videoService;

    // Record a watch event. The trigger on watch_history increments
    // video.views_count.
    public WatchHistory record(Long userId, Long videoId, WatchRequest req) {
        User user = userService.findById(userId);
        Video video = videoService.findById(videoId);

        WatchHistory h = WatchHistory.builder()
                .user(user)
                .video(video)
                .watchDuration(req.watchDuration() != null ? req.watchDuration() : 0)
                .progressPct(req.progressPct() != null ? req.progressPct() : BigDecimal.ZERO)
                .deviceType(req.deviceType())
                .build();
        return historyRepository.save(h);
    }

    @Transactional(readOnly = true)
    public Page<WatchHistory> history(Long userId, Pageable pageable) {
        return historyRepository.findByUserUserIdOrderByWatchedAtDesc(userId, pageable);
    }
}
