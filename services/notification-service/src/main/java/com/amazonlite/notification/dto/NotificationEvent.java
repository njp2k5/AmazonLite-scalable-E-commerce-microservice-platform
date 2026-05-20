package com.amazonlite.notification.dto;

import java.time.Instant;

public record NotificationEvent(
        String type,
        String message,
        Instant createdAt
) {
}
