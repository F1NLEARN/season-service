package com.finlearn.seasonservice.infrastructure.kafka;

import com.finlearn.seasonservice.domain.event.SeasonEndedEvent;
import com.finlearn.seasonservice.domain.event.SeasonStartedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

// 시즌 도메인 이벤트 Kafka 발행 클래스
@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonEventProducer {

    private static final String TOPIC_SEASON_STARTED = "season.started";
    private static final String TOPIC_SEASON_ENDED = "season.ended";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishSeasonStarted(SeasonStartedEvent event) {
        log.info("[Kafka] SeasonStarted 이벤트 발행: seasonId={}, seasonNumber={}",
                event.getSeasonId(), event.getSeasonNumber());
        kafkaTemplate.send(TOPIC_SEASON_STARTED, event.getSeasonId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] SeasonStarted 이벤트 발행 실패: {}", ex.getMessage());
                    } else {
                        log.debug("[Kafka] SeasonStarted 이벤트 발행 완료: offset={}",
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishSeasonEnded(SeasonEndedEvent event) {
        log.info("[Kafka] SeasonEnded 이벤트 발행: seasonId={}, seasonNumber={}",
                event.getSeasonId(), event.getSeasonNumber());
        kafkaTemplate.send(TOPIC_SEASON_ENDED, event.getSeasonId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("[Kafka] SeasonEnded 이벤트 발행 실패: {}", ex.getMessage());
                    } else {
                        log.debug("[Kafka] SeasonEnded 이벤트 발행 완료: offset={}",
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
