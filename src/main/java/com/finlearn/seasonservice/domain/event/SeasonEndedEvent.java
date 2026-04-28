package com.finlearn.seasonservice.domain.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

// 시즌이 ACTIVE → ENDED로 전환될 때 발생하는 도메인 이벤트
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SeasonEndedEvent {

    private UUID seasonId;
    private Integer seasonNumber;
}
