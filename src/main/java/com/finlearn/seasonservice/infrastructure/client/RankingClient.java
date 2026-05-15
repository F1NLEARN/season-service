package com.finlearn.seasonservice.infrastructure.client;

import com.finlearn.seasonservice.infrastructure.client.dto.RankingGradeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingClient {

    private final RestTemplate restTemplate;

    @Value("${internal.ranking-service.url}")
    private String rankingServiceUrl;

    /**
     * 시즌 종료 후 확정된 유저의 ALL 랭킹 순위 조회
     * ranking-service 내부 API: GET /internal/v1/rankings/seasons/{seasonId}/rank?userId={userId}
     * 랭킹 확정 전이거나 조회 실패 시 null 반환 → 보너스 0 적용
     */
    public Integer getRank(UUID userId, UUID seasonId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(rankingServiceUrl)
                    .path("/internal/v1/rankings/seasons/{seasonId}/rank")
                    .queryParam("userId", userId.toString())
                    .buildAndExpand(seasonId.toString())
                    .toUriString();

            ResponseEntity<RankingGradeResponse> response =
                    restTemplate.getForEntity(url, RankingGradeResponse.class);

            RankingGradeResponse body = response.getBody();
            if (body == null) {
                return null;
            }
            return body.getRank();
        } catch (RestClientException e) {
            log.warn("[RankingClient] 랭킹 순위 조회 실패 (userId={}, seasonId={}): {}",
                    userId, seasonId, e.getMessage());
            return null;
        }
    }
}
