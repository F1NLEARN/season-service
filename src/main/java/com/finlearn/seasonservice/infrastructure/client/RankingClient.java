package com.finlearn.seasonservice.infrastructure.client;

import com.finlearn.seasonservice.infrastructure.client.dto.RankingGradeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
     * TODO: ranking-service 내부 API 구현
     * 특정 시즌, 유저의 ALL 랭킹 순위 조회
     * ranking-service 내부 API 구현 전까지 항상 null 반환 (보너스 0 적용)
     */
    public Integer getRank(UUID userId, UUID seasonId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(rankingServiceUrl)
                    .path("/api/v1/rankings/seasons/{seasonId}/me")
                    .buildAndExpand(seasonId.toString())
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId.toString());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<RankingGradeResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, RankingGradeResponse.class);

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
