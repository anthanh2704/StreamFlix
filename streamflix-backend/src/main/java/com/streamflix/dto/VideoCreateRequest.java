package com.streamflix.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VideoCreateRequest(
                @NotBlank @Size(max = 200) String title,
                String description,
                @NotBlank String videoUrl,
                String thumbnailUrl,
                @Positive Integer durationSec,
                String resolution,
                Boolean isPremium,
                List<Integer> categoryIds,
                List<String> tags) {
}
