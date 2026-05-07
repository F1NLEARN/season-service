package com.finlearn.seasonservice.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finlearn.seasonservice.application.SeasonService;
import com.finlearn.seasonservice.infrastructure.kafka.event.RankingFinalizedEvent;
import com.finlearn.seasonservice.infrastructure.kafka.event.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonEventConsumer {

    private final SeasonService seasonService;
    private final ObjectMapper objectMapper;

    /**
     * ranking.finalized 이벤트 수신
     * 랭킹 확정 완료 후 다음 시즌 참여자 시드머니 보너스 산정
     */
    @KafkaListener(
            topics = "${kafka.topics.ranking.finalized}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleRankingFinalized(Map<String, Object> payload) {
        try {
            log.info("[Kafka] RankingFinalized 이벤트 수신");
            RankingFinalizedEvent event = objectMapper.convertValue(payload, RankingFinalizedEvent.class);
            seasonService.processRankingFinalized(event.getSeasonId());
        } catch (Exception e) {
            log.error("[Kafka] RankingFinalized 이벤트 처리 실패 - 재시도 유도: {}", e.getMessage(), e);
            throw new RuntimeException("[Kafka] RankingFinalized 처리 실패", e);
        }
    }

    /**
     * user.profile-updated 이벤트 수신
     * season_participants의 user_nickname VO 스냅샷 갱신
     */
    @KafkaListener(
            topics = "${kafka.topics.user.updated}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void handleUserProfileUpdated(Map<String, Object> payload) {
        try {
            log.info("[Kafka] UserProfileUpdated 이벤트 수신");
            UserProfileUpdatedEvent event = objectMapper.convertValue(payload, UserProfileUpdatedEvent.class);
            seasonService.syncUserProfile(event.getUserId(), event.getNickname());
        } catch (Exception e) {
            log.error("[Kafka] UserProfileUpdated 이벤트 처리 실패 - 재시도 유도: {}", e.getMessage(), e);
            throw new RuntimeException("[Kafka] UserProfileUpdated 처리 실패", e);
        }
    }
}
