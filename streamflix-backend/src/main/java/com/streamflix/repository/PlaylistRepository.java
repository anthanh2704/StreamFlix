package com.streamflix.repository;

import com.streamflix.entity.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {
    List<Playlist> findByUserUserId(Long userId);
    List<Playlist> findByUserUserIdAndIsPublicTrue(Long userId);
}
