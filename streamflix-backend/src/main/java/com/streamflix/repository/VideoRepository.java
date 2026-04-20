package com.streamflix.repository;

import com.streamflix.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {

       // Browse published videos, newest first
       Page<Video> findByStatus(Video.Status status, Pageable pageable);

       // Search by title or description (case-insensitive)
       @Query("""
                     SELECT v FROM Video v
                     WHERE v.status = com.streamflix.entity.Video.Status.PUBLISHED
                       AND (LOWER(v.title) LIKE LOWER(CONCAT('%', :q, '%'))
                            OR LOWER(v.description) LIKE LOWER(CONCAT('%', :q, '%')))
                     """)
       Page<Video> search(@Param("q") String query, Pageable pageable);

       // Trending — most viewed in last N days
       @Query(value = """
                     SELECT * FROM video
                     WHERE status = 'PUBLISHED'
                       AND upload_date >= DATE_SUB(NOW(), INTERVAL :days DAY)
                     ORDER BY views_count DESC, likes_count DESC
                     LIMIT :limit
                     """, nativeQuery = true)
       List<Video> findTrending(@Param("days") int days, @Param("limit") int limit);

       // Videos from a channel
       Page<Video> findByChannelChannelIdAndStatus(Long channelId,
                     Video.Status status,
                     Pageable pageable);

       // Videos by category (M:N)
       @Query("""
                     SELECT v FROM Video v JOIN v.categories c
                     WHERE c.categoryId = :categoryId
                       AND v.status = com.streamflix.entity.Video.Status.PUBLISHED
                     """)
       Page<Video> findByCategory(@Param("categoryId") Integer categoryId, Pageable pageable);

       // Recommendations: users who watched the same videos as the current user also
       // watched ...
       @Query(value = """
                     SELECT v.* FROM video v
                     WHERE v.video_id IN (
                         SELECT h2.video_id FROM watch_history h2
                         WHERE h2.user_id IN (
                             SELECT DISTINCT h1.user_id FROM watch_history h1
                             WHERE h1.video_id IN (SELECT video_id FROM watch_history WHERE user_id = :userId)
                               AND h1.user_id <> :userId
                         )
                         AND h2.video_id NOT IN (SELECT video_id FROM watch_history WHERE user_id = :userId)
                     )
                     AND v.status = 'PUBLISHED'
                     ORDER BY v.views_count DESC
                     LIMIT :limit
                     """, nativeQuery = true)
       List<Video> findRecommendations(@Param("userId") Long userId, @Param("limit") int limit);
}
