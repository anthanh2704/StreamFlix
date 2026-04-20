package com.streamflix.service;

import com.streamflix.dto.VideoCreateRequest;
import com.streamflix.entity.*;
import com.streamflix.exception.BadRequestException;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class VideoService {

    private final VideoRepository         videoRepository;
    private final ChannelRepository       channelRepository;
    private final CategoryRepository      categoryRepository;
    private final TagRepository           tagRepository;

    /** Upload a new video for a given channel. */
    public Video uploadVideo(Long channelId, VideoCreateRequest req) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", channelId));

        if (req.durationSec() == null || req.durationSec() <= 0)
            throw new BadRequestException("Duration must be > 0");

        Video v = Video.builder()
                .channel(channel)
                .title(req.title())
                .description(req.description())
                .videoUrl(req.videoUrl())
                .thumbnailUrl(req.thumbnailUrl())
                .durationSec(req.durationSec())
                .resolution(req.resolution() != null ? req.resolution() : "HD")
                .isPremium(Boolean.TRUE.equals(req.isPremium()))
                .status(Video.Status.PUBLISHED)
                .viewsCount(0L).likesCount(0L).dislikesCount(0L)
                .build();

        // categories
        if (req.categoryIds() != null && !req.categoryIds().isEmpty()) {
            Set<Category> cats = new HashSet<>(categoryRepository.findAllById(req.categoryIds()));
            v.setCategories(cats);
        }

        // tags (upsert unknowns)
        if (req.tags() != null && !req.tags().isEmpty()) {
            Set<Tag> tags = new HashSet<>();
            for (String name : req.tags()) {
                Tag t = tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(Tag.builder().name(name).build()));
                tags.add(t);
            }
            v.setTags(tags);
        }

        return videoRepository.save(v);
    }

    @Transactional(readOnly = true)
    public Video findById(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Video", id));
    }

    @Transactional(readOnly = true)
    public Page<Video> listPublished(Pageable pageable) {
        return videoRepository.findByStatus(Video.Status.PUBLISHED, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Video> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) return listPublished(pageable);
        return videoRepository.search(q.trim(), pageable);
    }

    @Transactional(readOnly = true)
    public List<Video> trending(int days, int limit) {
        return videoRepository.findTrending(days, limit);
    }

    @Transactional(readOnly = true)
    public List<Video> recommendations(Long userId, int limit) {
        return videoRepository.findRecommendations(userId, limit);
    }

    @Transactional(readOnly = true)
    public Page<Video> byCategory(Integer categoryId, Pageable pageable) {
        return videoRepository.findByCategory(categoryId, pageable);
    }

    public void delete(Long id) {
        Video v = findById(id);
        v.setStatus(Video.Status.REMOVED);       // soft delete preserves analytics
        videoRepository.save(v);
    }
}
