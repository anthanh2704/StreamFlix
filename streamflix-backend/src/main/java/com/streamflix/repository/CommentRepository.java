package com.streamflix.repository;

import com.streamflix.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Top-level comments on a video (no parent)
    @Query("""
           SELECT c FROM Comment c
           WHERE c.video.videoId = :videoId AND c.parent IS NULL
           ORDER BY c.createdAt DESC
           """)
    Page<Comment> findTopLevelByVideo(@Param("videoId") Long videoId, Pageable pageable);

    // Replies to a given comment
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
}
