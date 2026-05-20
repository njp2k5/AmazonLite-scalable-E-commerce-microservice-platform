package com.amazonlite.order.event;

import com.amazonlite.shared.events.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import java.util.concurrent.CompletableFuture;

@Component
public class OrderEventPublisher {

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;
    private final String topic;
    private final Logger logger = LoggerFactory.getLogger(OrderEventPublisher.class);

    public OrderEventPublisher(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate,
                               @Value("${app.kafka.order-created-topic:order.created}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        try {
            CompletableFuture<SendResult<String, OrderCreatedEvent>> future = kafkaTemplate.send(topic, event);
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("{" +
                            "\"eventType\": \"OrderCreated\", " +
                            "\"topic\": \"{}\", " +
                            "\"orderId\": \"{}\", " +
                            "\"timestamp\": \"{}\" " +
                            "}", topic, event.getOrderId(), System.currentTimeMillis());
                } else {
                    logger.error("{" +
                            "\"eventType\": \"OrderCreated\", " +
                            "\"topic\": \"{}\", " +
                            "\"orderId\": \"{}\", " +
                            "\"timestamp\": \"{}\", " +
                            "\"error\": \"{}\" " +
                            "}", topic, event.getOrderId(), System.currentTimeMillis(), ex.getMessage());
                }
            });
        } catch (Exception ex) {
            logger.error("{" +
                    "\"eventType\": \"OrderCreated\", " +
                    "\"topic\": \"{}\", " +
                    "\"orderId\": \"{}\", " +
                    "\"timestamp\": \"{}\", " +
                    "\"error\": \"{}\" " +
                    "}", topic, event.getOrderId(), System.currentTimeMillis(), ex.getMessage());
        }
    }
}
