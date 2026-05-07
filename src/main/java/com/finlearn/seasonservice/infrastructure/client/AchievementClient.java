package com.finlearn.seasonservice.infrastructure.client;

import com.finlearn.seasonservice.infrastructure.client.dto.AchievementCountResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
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

    // 업적 달성 수 조회 (조회 실패 시 0 반환)
    public int getAchievementCount(UUID userId, UUID seasonId) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(achievementServiceUrl)
                    .path("/internal/v1/achievements/count")
                    .queryParam("userId", userId.toString())
                    .queryParam("seasonId", seasonId.toString())
                    .build()
                    .toUriString();

            ResponseEntity<AchievementCountResponse> response =
                    restTemplate.exchange(url, HttpMethod.GET, HttpEntity.EMPTY, AchievementCountResponse.class);

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
