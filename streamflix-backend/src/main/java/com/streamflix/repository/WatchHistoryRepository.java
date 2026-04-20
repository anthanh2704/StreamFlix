package com.streamflix.repository;

import com.streamflix.entity.WatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, Long> {
    Page<WatchHistory> findByUserUserIdOrderByWatchedAtDesc(Long userId, Pageable pageable);
}
