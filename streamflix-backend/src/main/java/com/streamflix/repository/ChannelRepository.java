package com.streamflix.repository;

import com.streamflix.entity.Channel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChannelRepository extends JpaRepository<Channel, Long> {
    Optional<Channel> findByOwnerUserId(Long ownerUserId);

    Optional<Channel> findByChannelName(String channelName);
}
