package com.amazonlite.notification.consumer;

import com.amazonlite.shared.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedEventConsumer {
    private static final Logger logger = LoggerFactory.getLogger(OrderCreatedEventConsumer.class);

    @KafkaListener(topics = "order.created", groupId = "notification-service-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(OrderCreatedEvent event) {
        String corr = event.getCorrelationId();
        org.slf4j.MDC.put("correlationId", corr);
        try {
            logger.info("{" +
                    "\"correlationId\": \"{}\", " +
                    "\"eventType\": \"OrderCreated\", " +
                    "\"topic\": \"order.created\", " +
                    "\"orderId\": \"{}\", " +
                    "\"timestamp\": \"{}\" " +
                    "}",
                    corr, event.getOrderId(), System.currentTimeMillis());

            // Simulate sending email notification
            logger.info("Sending order confirmation for order {} to user {}", event.getOrderId(), event.getUserId());
        } finally {
            org.slf4j.MDC.remove("correlationId");
        }
    }
}
