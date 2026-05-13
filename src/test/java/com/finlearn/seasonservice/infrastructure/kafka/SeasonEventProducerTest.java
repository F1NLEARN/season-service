package com.finlearn.seasonservice.infrastructure.kafka;

import com.finlearn.seasonservice.domain.event.SeasonEndedEvent;
import com.finlearn.seasonservice.domain.event.SeasonStartedEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.stereotype.Component;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("kafka-test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-season-started", "test-season-ended"}
)
@DirtiesContext
@Import(SeasonEventProducerTest.TestKafkaConsumer.class)
@DisplayName("[Kafka] SeasonEventProducer 통합 테스트")
class SeasonEventProducerTest {

    @Autowired
    private SeasonEventProducer producer;

    @Autowired
    private TestKafkaConsumer testConsumer;

    @Autowired
    private KafkaListenerEndpointRegistry listenerRegistry;

    /** 테스트용 컨슈머가 파티션에 할당될 때까지 대기 */
    @BeforeEach
    void waitForConsumerReady() throws Exception {
        for (var container : listenerRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, 1);
        }
    }

    @Test
    @DisplayName("SeasonStarted 이벤트 발행 시 test-season-started 토픽에서 수신된다")
    void publishSeasonStarted_messageReceivedOnTopic() throws InterruptedException {
        // given
        SeasonStartedEvent event = new SeasonStartedEvent(
                UUID.randomUUID(), 3, LocalDate.now(), LocalDate.now().plusMonths(6));

        // when
        producer.publishSeasonStarted(event);

        // then: 최대 5초 대기
        boolean received = testConsumer.startedLatch.await(5, TimeUnit.SECONDS);
        assertThat(received).isTrue();
        assertThat(testConsumer.lastStartedRecord).isNotNull();
    }

    @Test
    @DisplayName("SeasonEnded 이벤트 발행 시 test-season-ended 토픽에서 수신된다")
    void publishSeasonEnded_messageReceivedOnTopic() throws InterruptedException {
        // given
        SeasonEndedEvent event = new SeasonEndedEvent(UUID.randomUUID(), 2);

        // when
        producer.publishSeasonEnded(event);

        // then: 최대 5초 대기
        boolean received = testConsumer.endedLatch.await(5, TimeUnit.SECONDS);
        assertThat(received).isTrue();
        assertThat(testConsumer.lastEndedRecord).isNotNull();
    }

    // ─────────────────────────────────────────────────────────────
    // 이벤트 수신 여부 확인용
    // ─────────────────────────────────────────────────────────────

    @Component
    static class TestKafkaConsumer {

        final CountDownLatch startedLatch = new CountDownLatch(1);
        final CountDownLatch endedLatch   = new CountDownLatch(1);

        ConsumerRecord<?, ?> lastStartedRecord;
        ConsumerRecord<?, ?> lastEndedRecord;

        @KafkaListener(topics = "test-season-started", groupId = "test-producer-group")
        void onSeasonStarted(ConsumerRecord<?, ?> record) {
            lastStartedRecord = record;
            startedLatch.countDown();
        }

        @KafkaListener(topics = "test-season-ended", groupId = "test-producer-group")
        void onSeasonEnded(ConsumerRecord<?, ?> record) {
            lastEndedRecord = record;
            endedLatch.countDown();
        }
    }
}
