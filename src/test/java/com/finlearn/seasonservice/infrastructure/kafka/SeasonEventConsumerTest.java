package com.finlearn.seasonservice.infrastructure.kafka;

import com.finlearn.seasonservice.application.SeasonService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@SpringBootTest
@ActiveProfiles("kafka-test")
@EmbeddedKafka(
        partitions = 1,
        topics = {"test-ranking-finalized", "test-user-updated"}
)
@DirtiesContext
@Import(SeasonEventProducerTest.TestKafkaConsumer.class)
@DisplayName("[Kafka] SeasonEventConsumer 통합 테스트")
class SeasonEventConsumerTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockitoBean
    private SeasonService seasonService;

    private static final UUID SEASON_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID USER_ID   = UUID.fromString("00000000-0000-0000-0001-000000000001");

    @Test
    @DisplayName("ranking.finalized 토픽 수신 시 processRankingFinalized() 가 호출된다")

    void handleRankingFinalized_callsProcessRankingFinalized() {
        // given
        Map<String, Object> payload = Map.of(
                "seasonId", SEASON_ID.toString(),
                "seasonNumber", 1
        );

        // when
        kafkaTemplate.send("test-ranking-finalized", SEASON_ID.toString(), payload);

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(seasonService).processRankingFinalized(SEASON_ID));
    }

    @Test
    @DisplayName("user.profile-updated 토픽 수신 시 syncUserProfile() 이 호출된다")
    void handleUserProfileUpdated_callsSyncUserProfile() {
        // given
        Map<String, Object> payload = Map.of(
                "userId", USER_ID.toString(),
                "nickname", "새닉네임",
                "profileImage", "https://example.com/image.png"
        );

        // when
        kafkaTemplate.send("test-user-updated", USER_ID.toString(), payload);

        // then
        await().atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(seasonService).syncUserProfile(eq(USER_ID), eq("새닉네임")));
    }
}
