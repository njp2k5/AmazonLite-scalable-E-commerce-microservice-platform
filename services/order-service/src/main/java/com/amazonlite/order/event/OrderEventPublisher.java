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
            ListenableFuture<SendResult<String, OrderCreatedEvent>> future = kafkaTemplate.send(topic, event);
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(SendResult<String, OrderCreatedEvent> result) {
                    if (result != null && result.getRecordMetadata() != null) {
                        logger.info("event=OrderCreatedPublished orderId={} partition={} offset={}", event.getOrderId(), result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    } else {
                        logger.info("event=OrderCreatedPublished orderId={} (no metadata)", event.getOrderId());
                    }
                }

                @Override
                public void onFailure(Throwable ex) {
                    logger.error("event=OrderCreatedPublishFailed orderId={}", event.getOrderId(), ex);
                }
            });
        } catch (Exception ex) {
            logger.error("event=OrderCreatedPublishException orderId={}", event.getOrderId(), ex);
        }
    }
}
