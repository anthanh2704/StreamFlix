package com.streamflix.dto;

import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record WatchRequest(
        @PositiveOrZero Integer watchDuration,
        BigDecimal progressPct,
        String deviceType) {
}
