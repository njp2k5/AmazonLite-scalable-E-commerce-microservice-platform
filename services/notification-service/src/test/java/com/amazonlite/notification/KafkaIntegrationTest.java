package com.amazonlite.notification;

import com.amazonlite.shared.events.OrderCreatedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(topics = {"order.created", "order.created.dlt"}, partitions = 1)
@ActiveProfiles("test")
public class KafkaIntegrationTest {

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    @Autowired
    KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @Autowired
    TestListeners listeners;

    @BeforeEach
    void setup() {
        listeners.reset();
    }

    @AfterEach
    void tearDown() {
        // noop
    }

    @Test
    void testOrderCreatedPublishedAndConsumed() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent("123", "u1", BigDecimal.TEN, Instant.now(), "corr-1");
        kafkaTemplate.send("order.created", event).get();

        boolean received = listeners.successLatch.await(10, TimeUnit.SECONDS);
        assertThat(received).isTrue();
    }

    @Test
    void testFailedEventRoutedToDLT() throws Exception {
        OrderCreatedEvent event = new OrderCreatedEvent("fail", "u2", BigDecimal.ONE, Instant.now(), "corr-2");
        kafkaTemplate.send("order.created", event).get();

        boolean dltReceived = listeners.dltLatch.await(15, TimeUnit.SECONDS);
        assertThat(dltReceived).isTrue();
    }

    @Configuration
    static class TestConfig {

        @Bean
        public TestListeners listeners() {
            return new TestListeners();
        }

        @Bean
        public ProducerFactory<String, OrderCreatedEvent> producerFactory(EmbeddedKafkaBroker embeddedKafka) {
            Map<String, Object> props = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafka));
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        public KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate(ProducerFactory<String, OrderCreatedEvent> pf) {
            return new KafkaTemplate<>(pf);
        }

        @Bean(name = "kafkaListenerContainerFactory")
        public ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> kafkaListenerContainerFactory(EmbeddedKafkaBroker embeddedKafka, KafkaTemplate<String, OrderCreatedEvent> kt) {
            Map<String, Object> props = new HashMap<>(KafkaTestUtils.consumerProps("testGroup", "false", embeddedKafka));
            props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
            props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

            DefaultKafkaConsumerFactory<String, OrderCreatedEvent> cf = new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(OrderCreatedEvent.class, false));

            ConcurrentKafkaListenerContainerFactory<String, OrderCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(cf);

            // Error handler to publish to DLT topic named <topic>.dlt
            DeadLetterPublishingRecovererForTest recoverer = new DeadLetterPublishingRecovererForTest(kt);
            DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new org.springframework.util.backoff.FixedBackOff(0L, 0L));
            factory.setCommonErrorHandler(handler);

            return factory;
        }
    }

    public static class TestListeners {
        CountDownLatch successLatch = new CountDownLatch(1);
        CountDownLatch dltLatch = new CountDownLatch(1);

        void reset() {
            successLatch = new CountDownLatch(1);
            dltLatch = new CountDownLatch(1);
        }

        @KafkaListener(topics = "order.created", groupId = "test-group")
        public void listen(OrderCreatedEvent event) {
            if ("fail".equals(event.getOrderId())) {
                throw new RuntimeException("simulated failure");
            }
            successLatch.countDown();
        }

        @KafkaListener(topics = "order.created.dlt", groupId = "test-group-dlt")
        public void dltListen(org.apache.kafka.clients.consumer.ConsumerRecord<String, OrderCreatedEvent> record) {
            // record value is null when using headers-only DLT; but our recoverer publishes payload
            if (record != null && record.value() != null && "fail".equals(record.value().getOrderId())) {
                dltLatch.countDown();
            }
        }
    }

    static class DeadLetterPublishingRecovererForTest extends org.springframework.kafka.listener.DeadLetterPublishingRecoverer {
        public DeadLetterPublishingRecovererForTest(KafkaTemplate<String, OrderCreatedEvent> template) {
            super(template, (r, e) -> new TopicPartition(r.topic() + ".dlt", r.partition()));
        }
    }
}
