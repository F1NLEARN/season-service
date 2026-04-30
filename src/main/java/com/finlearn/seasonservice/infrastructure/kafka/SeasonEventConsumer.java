package com.finlearn.seasonservice.infrastructure.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finlearn.seasonservice.application.SeasonService;
import com.finlearn.seasonservice.infrastructure.kafka.event.RankingFinalizedEvent;
import com.finlearn.seasonservice.infrastructure.kafka.event.UserProfileUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 시즌 도메인 이벤트 Kafka 수신 클래스
 *
 * common KafkaConfig의 JsonDeserializer<Object> 기반으로 메시지를 수신
 * 리스너 메서드는 Object 타입으로 받은 뒤 ObjectMapper.convertValue()로 이벤트 타입으로 변환
 */
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
    @KafkaListener(topics = "ranking.finalized", groupId = "season-service")
    public void handleRankingFinalized(Object payload) {
        try {
            log.info("[Kafka] RankingFinalized 이벤트 수신");
            RankingFinalizedEvent event = objectMapper.convertValue(payload, RankingFinalizedEvent.class);
            seasonService.processRankingFinalized(event.getSeasonId());
        } catch (Exception e) {
            log.error("[Kafka] RankingFinalized 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * user.profile-updated 이벤트 수신
     * 닉네임 변경 시 season_participants의 user_nickname VO 스냅샷 갱신
     */
    @KafkaListener(topics = "user.profile-updated", groupId = "season-service")
    public void handleUserProfileUpdated(Object payload) {
        try {
            log.info("[Kafka] UserProfileUpdated 이벤트 수신");
            UserProfileUpdatedEvent event = objectMapper.convertValue(payload, UserProfileUpdatedEvent.class);
            seasonService.syncUserNickname(event.getUserId(), event.getNickname());
        } catch (Exception e) {
            log.error("[Kafka] UserProfileUpdated 이벤트 처리 실패: {}", e.getMessage(), e);
        }
    }
}
