package com.amazonlite.notification.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterTopicConsumer {
    private static final Logger logger = LoggerFactory.getLogger(DeadLetterTopicConsumer.class);

    @KafkaListener(topics = "order.created.dlt", groupId = "notification-service-dlt-group")
    public void consumeDlt(ConsumerRecord<String, Object> record) {
        String payload = record.value() != null ? record.value().toString() : "null";
        Exception exception = (Exception) record.headers().lastHeader("kafka_dlt-exception-message") != null ?
                new Exception(new String(record.headers().lastHeader("kafka_dlt-exception-message").value())) : null;
        String reason = exception != null ? exception.getMessage() : "Unknown";
        logger.error("[DLT] Consumed failed message from order.created.dlt | Reason: {} | Payload: {} | Headers: {}", reason, payload, record.headers());
    }
}
