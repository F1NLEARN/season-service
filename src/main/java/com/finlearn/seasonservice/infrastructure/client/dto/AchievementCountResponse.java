package com.finlearn.seasonservice.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AchievementCountResponse {

    private UUID userId;
    private UUID seasonId;
    private Integer achievementCount;
}
