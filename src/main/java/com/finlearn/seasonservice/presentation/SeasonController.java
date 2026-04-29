package com.finlearn.seasonservice.presentation;

import com.finlearn.common.exception.BadRequestException;
import com.finlearn.seasonservice.application.SeasonService;
import com.finlearn.seasonservice.domain.Season;
import com.finlearn.seasonservice.domain.SeasonParticipant;
import com.finlearn.seasonservice.domain.vo.PassedCategory;
import com.finlearn.seasonservice.presentation.dto.request.RegisterParticipantRequest;
import com.finlearn.seasonservice.presentation.dto.response.SeasonListResponse;
import com.finlearn.seasonservice.presentation.dto.response.SeasonParticipantResponse;
import com.finlearn.seasonservice.presentation.dto.response.SeasonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/seasons")
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonService seasonService;

    /**
     * GET /api/v1/seasons
     * 전체 시즌 목록 조회
     */
    @GetMapping
    public SeasonListResponse getAllSeasons() {
        List<Season> seasons = seasonService.getAllSeasons();
        List<SeasonResponse> responses = seasons.stream()
                .map(this::toSeasonResponse)
                .toList();
        return new SeasonListResponse(responses);
    }

    /**
     * GET /api/v1/seasons/current
     * 현재 진행 중인 시즌 조회
     */
    @GetMapping("/current")
    public SeasonResponse getCurrentSeason() {
        return toSeasonResponse(seasonService.getCurrentSeason());
    }

    /**
     * GET /api/v1/seasons/{seasonId}/me
     * 내 시즌 참여 정보 조회
     * Gateway에서 X-User-Id 헤더로 유저 ID 전달
     */
    @GetMapping("/{seasonId}/me")
    public SeasonParticipantResponse getMyParticipation(
            @PathVariable UUID seasonId,
            @RequestHeader("X-User-Id") UUID userId
    ) {
        return toParticipantResponse(seasonService.getMyParticipation(seasonId, userId));
    }

    /**
     * POST /api/v1/seasons/{seasonId}/participants
     * 시즌 참여 등록
     * 포인트 퀴즈 통과 후 퀴즈 서비스 또는 유저가 직접 호출
     */
    @PostMapping("/{seasonId}/participants")
    @ResponseStatus(HttpStatus.CREATED)
    public SeasonParticipantResponse registerParticipant(
            @PathVariable UUID seasonId,
            @RequestHeader("X-User-Id") UUID userId,
            @RequestHeader("X-User-Nickname") String userNickname,
            @Valid @RequestBody RegisterParticipantRequest request
    ) {
        List<PassedCategory> parsedCategories = parseCategories(request.getPassedCategories());
        SeasonParticipant participant = seasonService.registerParticipant(
                seasonId, userId, userNickname, parsedCategories
        );
        return toParticipantResponse(participant);
    }

    // ────────────────────────────────────────────────────────────────
    // DTO 변환 (Presentation 계층 책임)
    // ────────────────────────────────────────────────────────────────

    private SeasonResponse toSeasonResponse(Season season) {
        return SeasonResponse.builder()
                .seasonId(season.getSeasonId())
                .seasonNumber(season.getSeasonNumber())
                .startDate(season.getStartDate())
                .endDate(season.getEndDate())
                .status(season.getStatus())
                .build();
    }

    private SeasonParticipantResponse toParticipantResponse(SeasonParticipant participant) {
        List<String> categories = participant.getParsedCategories().stream()
                .map(PassedCategory::name)
                .toList();

        return SeasonParticipantResponse.builder()
                .seasonParticipantId(participant.getSeasonParticipantId())
                .seasonId(participant.getSeason().getSeasonId())
                .userId(participant.getUserId().getValue())
                .passedCategories(categories)
                .baseSeedMoney(participant.getBaseSeedMoney())
                .achievementBonus(participant.getAchievementBonus())
                .rankingBonus(participant.getRankingBonus())
                .totalSeedMoney(participant.getTotalSeedMoney())
                .paidAt(participant.getPaidAt())
                .build();
    }

    private List<PassedCategory> parseCategories(List<String> rawCategories) {
        try {
            return rawCategories.stream()
                    .map(PassedCategory::valueOf)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("유효하지 않은 카테고리입니다. 허용값: STOCK, ETF");
        }
    }
}
