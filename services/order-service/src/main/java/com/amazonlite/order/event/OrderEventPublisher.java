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

    public void publishOrderCreated(OrderCreatedEvent event, String requestId) {
        try {
            ListenableFuture<SendResult<String, OrderCreatedEvent>> future = kafkaTemplate.send(topic, event);
            future.addCallback(new ListenableFutureCallback<>() {
                @Override
                public void onSuccess(SendResult<String, OrderCreatedEvent> result) {
                    if (result != null && result.getRecordMetadata() != null) {
                        logger.info("{" +
                                "\"requestId\": \"{}\", " +
                                "\"eventType\": \"OrderCreated\", " +
                                "\"topic\": \"{}\", " +
                                "\"orderId\": \"{}\", " +
                                "\"timestamp\": \"{}\" " +
                                "}", requestId, topic, event.getOrderId(), result.getRecordMetadata().timestamp());
                    } else {
                        logger.info("{" +
                                "\"requestId\": \"{}\", " +
                                "\"eventType\": \"OrderCreated\", " +
                                "\"topic\": \"{}\", " +
                                "\"orderId\": \"{}\", " +
                                "\"timestamp\": \"{}\" " +
                                "}", requestId, topic, event.getOrderId(), System.currentTimeMillis());
                    }
                }

                @Override
                public void onFailure(Throwable ex) {
                    logger.error("{" +
                            "\"requestId\": \"{}\", " +
                            "\"eventType\": \"OrderCreated\", " +
                            "\"topic\": \"{}\", " +
                            "\"orderId\": \"{}\", " +
                            "\"timestamp\": \"{}\", " +
                            "\"error\": \"{}\" " +
                            "}", requestId, topic, event.getOrderId(), System.currentTimeMillis(), ex.getMessage());
                }
            });
        } catch (Exception ex) {
            logger.error("{" +
                    "\"requestId\": \"{}\", " +
                    "\"eventType\": \"OrderCreated\", " +
                    "\"topic\": \"{}\", " +
                    "\"orderId\": \"{}\", " +
                    "\"timestamp\": \"{}\", " +
                    "\"error\": \"{}\" " +
                    "}", requestId, topic, event.getOrderId(), System.currentTimeMillis(), ex.getMessage());
        }
    }
}
