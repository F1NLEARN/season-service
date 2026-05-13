package com.finlearn.seasonservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * 랭킹 서비스로부터 수신
 * 시즌 종료 후 랭킹 확정 + 뱃지 지급 완료 시 발행
 * 수신 후: 다음 시즌 참여자 시드머니 산정 시작
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RankingFinalizedEvent {

    private UUID seasonId;
    private Integer seasonNumber;
}
