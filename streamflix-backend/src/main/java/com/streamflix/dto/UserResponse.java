package com.streamflix.dto;

import com.streamflix.entity.User;

import java.time.LocalDateTime;

public record UserResponse(
        Long userId,
        String username,
        String email,
        String fullName,
        String country,
        String avatarUrl,
        String role,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static UserResponse fromEntity(User u) {
        return new UserResponse(
                u.getUserId(),
                u.getUsername(),
                u.getEmail(),
                u.getFullName(),
                u.getCountry(),
                u.getAvatarUrl(),
                u.getRole() != null ? u.getRole().name() : null,
                u.getIsActive(),
                u.getCreatedAt());
    }
}
