package com.streamflix.service;

import com.streamflix.dto.CommentDto;
import com.streamflix.entity.Comment;
import com.streamflix.entity.User;
import com.streamflix.entity.Video;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService       userService;
    private final VideoService      videoService;

    public Comment post(Long userId, Long videoId, CommentDto.Request req) {
        User  user  = userService.findById(userId);
        Video video = videoService.findById(videoId);

        Comment parent = null;
        if (req.parentCommentId() != null) {
            parent = commentRepository.findById(req.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment", req.parentCommentId()));
        }

        Comment c = Comment.builder()
                .user(user).video(video).parent(parent)
                .content(req.content())
                .likesCount(0)
                .build();
        return commentRepository.save(c);
    }

    @Transactional(readOnly = true)
    public Page<Comment> topLevel(Long videoId, Pageable pageable) {
        return commentRepository.findTopLevelByVideo(videoId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Comment> replies(Long commentId) {
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
    }

    public void delete(Long commentId) {
        commentRepository.deleteById(commentId);
    }
}
