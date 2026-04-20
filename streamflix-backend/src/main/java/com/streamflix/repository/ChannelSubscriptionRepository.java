package com.streamflix.repository;

import com.streamflix.entity.ChannelSubscription;
import com.streamflix.entity.ChannelSubscription.ChannelSubscriptionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChannelSubscriptionRepository
        extends JpaRepository<ChannelSubscription, ChannelSubscriptionId> {

    List<ChannelSubscription> findBySubscriberUserId(Long userId);
    List<ChannelSubscription> findByChannelId(Long channelId);
    boolean existsBySubscriberUserIdAndChannelId(Long userId, Long channelId);
}
