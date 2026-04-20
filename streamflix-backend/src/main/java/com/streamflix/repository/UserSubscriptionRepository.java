package com.streamflix.repository;

import com.streamflix.entity.UserSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    List<UserSubscription> findByUserUserId(Long userId);

    Optional<UserSubscription> findFirstByUserUserIdAndStatusOrderByEndDateDesc(
            Long userId, UserSubscription.Status status);
}
