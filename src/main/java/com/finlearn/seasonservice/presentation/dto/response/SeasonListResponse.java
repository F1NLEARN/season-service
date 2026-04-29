package com.finlearn.seasonservice.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.util.List;

@Getter
@AllArgsConstructor
public class SeasonListResponse {

    private List<SeasonResponse> seasons;
}
