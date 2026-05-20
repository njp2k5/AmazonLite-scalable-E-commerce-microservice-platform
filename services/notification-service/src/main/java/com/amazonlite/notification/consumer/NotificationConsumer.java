package com.amazonlite.notification.consumer;

import com.amazonlite.notification.dto.NotificationEvent;
import com.amazonlite.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private final NotificationService notificationService;

    public NotificationConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "${app.kafka.notification-topic}")
    public void consume(NotificationEvent event) {
        notificationService.handleNotification(event);
    }
}
