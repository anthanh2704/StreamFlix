package com.streamflix.service;

import com.streamflix.entity.Channel;
import com.streamflix.entity.ChannelSubscription;
import com.streamflix.entity.User;
import com.streamflix.exception.BadRequestException;
import com.streamflix.exception.ResourceNotFoundException;
import com.streamflix.repository.ChannelRepository;
import com.streamflix.repository.ChannelSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ChannelService {

    private final ChannelRepository              channelRepository;
    private final ChannelSubscriptionRepository  subscriptionRepository;
    private final UserService                    userService;

    public Channel createChannel(Long ownerUserId, String name, String description) {
        User owner = userService.findById(ownerUserId);
        if (channelRepository.findByOwnerUserId(ownerUserId).isPresent())
            throw new BadRequestException("User already owns a channel");

        if (owner.getRole() == User.Role.USER)
            owner.setRole(User.Role.CREATOR);

        return channelRepository.save(Channel.builder()
                .owner(owner).channelName(name).description(description)
                .subscriberCount(0L).build());
    }

    @Transactional(readOnly = true)
    public Channel findById(Long id) {
        return channelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Channel", id));
    }

    @Transactional(readOnly = true)
    public List<Channel> all() { return channelRepository.findAll(); }

    public String subscribe(Long userId, Long channelId) {
        findById(channelId);
        userService.findById(userId);

        if (subscriptionRepository.existsBySubscriberUserIdAndChannelId(userId, channelId)) {
            subscriptionRepository.deleteById(
                    new ChannelSubscription.ChannelSubscriptionId(userId, channelId));
            return "UNSUBSCRIBED";
        }
        subscriptionRepository.save(ChannelSubscription.builder()
                .subscriberUserId(userId).channelId(channelId)
                .notificationsOn(true).build());
        return "SUBSCRIBED";
    }
}
