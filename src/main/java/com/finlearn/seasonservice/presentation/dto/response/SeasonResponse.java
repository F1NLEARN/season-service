package com.finlearn.seasonservice.presentation.dto.response;

import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class SeasonResponse {

    private UUID seasonId;
    private Integer seasonNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private SeasonStatus status;
}
