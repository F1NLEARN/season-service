package com.finlearn.seasonservice.presentation.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class SeasonParticipantResponse {

    private UUID seasonParticipantId;
    private UUID seasonId;
    private UUID userId;
    private List<String> passedCategories;
    private Integer baseSeedMoney;
    private Integer achievementBonus;
    private Integer rankingBonus;
    private Integer totalSeedMoney;
    private LocalDateTime paidAt;
}
