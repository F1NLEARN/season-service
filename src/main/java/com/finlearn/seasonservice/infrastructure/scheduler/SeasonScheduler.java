package com.finlearn.seasonservice.infrastructure.scheduler;

import com.finlearn.seasonservice.application.SeasonService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeasonScheduler {

    private final SeasonService seasonService;

    /**
     * 매일 자정에 실행되는 스케줄러
     * 순서 보장: 종료(ACTIVE → ENDED) 먼저 처리 후 시작(UPCOMING → ACTIVE) 처리
     * 같은 날 종료/시작이 겹치는 경우, 종료를 먼저 처리해야 새 시즌이 ACTIVE 상태가 됨
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void processSeasonTransitions() {
        log.info("[Scheduler] 시즌 전환 스케줄러 실행");

        // 이전 시즌 먼저 종료 처리
        try {
            int endedCount = seasonService.endActiveSeasons();
            log.info("[Scheduler] {}개 시즌 종료 처리 완료 (ACTIVE → ENDED)", endedCount);
        } catch (Exception e) {
            log.error("[Scheduler] 시즌 종료 처리 중 오류 발생: {}", e.getMessage(), e);
        }

        // 이후 새 시즌 시작 처리
        try {
            int startedCount = seasonService.startUpcomingSeasons();
            log.info("[Scheduler] {}개 시즌 시작 처리 완료 (UPCOMING → ACTIVE)", startedCount);
        } catch (Exception e) {
            log.error("[Scheduler] 시즌 시작 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}
