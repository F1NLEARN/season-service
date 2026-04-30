package com.finlearn.seasonservice.infrastructure.client;

import com.finlearn.seasonservice.infrastructure.client.dto.AchievementCountResponse;
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
public class AchievementClient {

    private final RestTemplate restTemplate;

    @Value("${internal.achievement-service.url}")
    private String achievementServiceUrl;

    /**
     * 특정 시즌에서 유저의 달성 업적 수 조회
     * GET /api/v1/achievements/me/count
     *
     * @param userId   유저 ID
     * @param seasonId 조회 대상 시즌 ID
     * @return 업적 달성 수 (조회 실패 시 0 반환)
     */
    public int getAchievementCount(UUID userId, UUID seasonId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(achievementServiceUrl)
                    .path("/api/v1/achievements/me/count")
                    .queryParam("seasonId", seasonId.toString())
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId.toString());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<AchievementCountResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, AchievementCountResponse.class);

            AchievementCountResponse body = response.getBody();
            if (body == null || body.getAchievementCount() == null) {
                return 0;
            }
            return body.getAchievementCount();
        } catch (RestClientException e) {
            log.warn("[AchievementClient] 업적 수 조회 실패 (userId={}, seasonId={}): {}",
                    userId, seasonId, e.getMessage());
            return 0;
        }
    }
}
