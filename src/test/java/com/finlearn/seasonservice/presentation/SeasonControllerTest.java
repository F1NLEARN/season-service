package com.finlearn.seasonservice.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finlearn.common.exception.GlobalExceptionAdviceImpl;
import com.finlearn.common.exception.NotFoundException;
import com.finlearn.common.response.CommonResponseAdvice;
import com.finlearn.seasonservice.application.SeasonService;
import com.finlearn.seasonservice.domain.Season;
import com.finlearn.seasonservice.domain.SeasonParticipant;
import com.finlearn.seasonservice.domain.vo.PassedCategory;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import com.finlearn.seasonservice.domain.vo.UserId;
import com.finlearn.seasonservice.presentation.dto.request.RegisterParticipantRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SeasonController.class)
@Import({GlobalExceptionAdviceImpl.class, CommonResponseAdvice.class})
@DisplayName("SeasonController")
class SeasonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SeasonService seasonService;

    private static final UUID SEASON_ID      = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID PARTICIPANT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID USER_ID        = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private Season stubSeason(UUID seasonId, int number, SeasonStatus status) {
        Season season = mock(Season.class);
        given(season.getSeasonId()).willReturn(seasonId);
        given(season.getSeasonNumber()).willReturn(number);
        given(season.getStartDate()).willReturn(LocalDate.of(2025, 1, 1));
        given(season.getEndDate()).willReturn(LocalDate.of(2025, 1, 31));
        given(season.getStatus()).willReturn(status);
        return season;
    }

    private SeasonParticipant stubParticipant(Season season) {
        SeasonParticipant participant = mock(SeasonParticipant.class);
        given(participant.getSeasonParticipantId()).willReturn(PARTICIPANT_ID);
        given(participant.getSeason()).willReturn(season);
        given(participant.getUserId()).willReturn(UserId.of(USER_ID));
        given(participant.getParsedCategories()).willReturn(List.of(PassedCategory.STOCK, PassedCategory.ETF));
        given(participant.getBaseSeedMoney()).willReturn(1000);
        given(participant.getAchievementBonus()).willReturn(0);
        given(participant.getRankingBonus()).willReturn(0);
        given(participant.getTotalSeedMoney()).willReturn(1000);
        given(participant.getPaidAt()).willReturn(LocalDateTime.of(2025, 1, 1, 0, 0));
        return participant;
    }

    // ────────────────────────────────────────────────────────────────
    // 시즌 조회 API
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/seasons")
    class GetAllSeasons {

        @Test
        @DisplayName("HTTP 200, 전체 시즌 목록이 반환된다")
        void getAllSeasons_HTTP200() throws Exception {
            Season s1 = stubSeason(SEASON_ID, 1, SeasonStatus.ACTIVE);
            given(seasonService.getAllSeasons()).willReturn(List.of(s1));

            mockMvc.perform(get("/api/v1/seasons"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("응답 필드 확인 - seasonId, seasonNumber, startDate, endDate, status")
        void getAllSeasons_응답_필드_확인() throws Exception {
            Season season = stubSeason(SEASON_ID, 1, SeasonStatus.ACTIVE);
            given(seasonService.getAllSeasons()).willReturn(List.of(season));

            mockMvc.perform(get("/api/v1/seasons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.seasons[0].seasonId")
                            .value(SEASON_ID.toString()))
                    .andExpect(jsonPath("$.data.seasons[0].seasonNumber").value(1))
                    .andExpect(jsonPath("$.data.seasons[0].startDate").value("2025-01-01"))
                    .andExpect(jsonPath("$.data.seasons[0].endDate").value("2025-01-31"))
                    .andExpect(jsonPath("$.data.seasons[0].status").value("ACTIVE"));
        }

        @Test
        @DisplayName("시즌이 없을 때 빈 배열이 반환된다")
        void getAllSeasons_빈_배열() throws Exception {
            given(seasonService.getAllSeasons()).willReturn(List.of());

            mockMvc.perform(get("/api/v1/seasons"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.seasons").isArray())
                    .andExpect(jsonPath("$.data.seasons").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/seasons/current")
    class GetCurrentSeason {

        @Test
        @DisplayName("HTTP 200, 현재 ACTIVE 시즌 정보가 반환된다")
        void getCurrentSeason_HTTP200() throws Exception {
            Season activeSeason = stubSeason(SEASON_ID, 2, SeasonStatus.ACTIVE);
            given(seasonService.getCurrentSeason()).willReturn(activeSeason);

            mockMvc.perform(get("/api/v1/seasons/current"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("응답 필드 확인 - seasonId, seasonNumber, startDate, endDate, status")
        void getCurrentSeason_응답_필드_확인() throws Exception {
            Season activeSeason = stubSeason(SEASON_ID, 2, SeasonStatus.ACTIVE);
            given(seasonService.getCurrentSeason()).willReturn(activeSeason);

            mockMvc.perform(get("/api/v1/seasons/current"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.seasonId").value(SEASON_ID.toString()))
                    .andExpect(jsonPath("$.data.seasonNumber").value(2))
                    .andExpect(jsonPath("$.data.startDate").value("2025-01-01"))
                    .andExpect(jsonPath("$.data.endDate").value("2025-01-31"))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("ACTIVE 시즌이 없으면 HTTP 404를 반환한다")
        void getCurrentSeason_없을때_HTTP404() throws Exception {
            given(seasonService.getCurrentSeason())
                    .willThrow(new NotFoundException("현재 진행 중인 시즌이 없습니다."));

            mockMvc.perform(get("/api/v1/seasons/current"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("현재 진행 중인 시즌이 없습니다."));
        }
    }

    // ────────────────────────────────────────────────────────────────
    // 시즌 참여 등록 및 조회 API
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/seasons/{seasonId}/participants")
    class RegisterParticipant {

        @Test
        @DisplayName("HTTP 201, 참여 등록 후 응답 필드가 일치한다")
        void registerParticipant_HTTP201_응답_필드_확인() throws Exception {
            Season season = stubSeason(SEASON_ID, 1, SeasonStatus.ACTIVE);
            SeasonParticipant participant = stubParticipant(season);
            given(seasonService.registerParticipant(
                    eq(SEASON_ID), eq(USER_ID), eq("테스트유저"), any()))
                    .willReturn(participant);

            RegisterParticipantRequest request = new RegisterParticipantRequest(List.of("STOCK", "ETF"));

            mockMvc.perform(post("/api/v1/seasons/{seasonId}/participants", SEASON_ID)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Nickname", "테스트유저")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.seasonParticipantId")
                            .value(PARTICIPANT_ID.toString()))
                    .andExpect(jsonPath("$.data.seasonId").value(SEASON_ID.toString()))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()))
                    .andExpect(jsonPath("$.data.passedCategories[0]").value("STOCK"))
                    .andExpect(jsonPath("$.data.passedCategories[1]").value("ETF"))
                    .andExpect(jsonPath("$.data.baseSeedMoney").value(1000))
                    .andExpect(jsonPath("$.data.achievementBonus").value(0))
                    .andExpect(jsonPath("$.data.rankingBonus").value(0))
                    .andExpect(jsonPath("$.data.totalSeedMoney").value(1000))
                    .andExpect(jsonPath("$.data.paidAt").isNotEmpty());
        }

        @Test
        @DisplayName("유효하지 않은 카테고리 입력 시 HTTP 400을 반환한다")
        void registerParticipant_유효하지_않은_카테고리_HTTP400() throws Exception {
            String body = """
                    {"passedCategories": ["BOND"]}
                    """;

            mockMvc.perform(post("/api/v1/seasons/{seasonId}/participants", SEASON_ID)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Nickname", "테스트유저")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message")
                            .value("유효하지 않은 카테고리입니다. 허용값: STOCK, ETF"));
        }

        @Test
        @DisplayName("passedCategories가 빈 배열이면 HTTP 400을 반환한다")
        void registerParticipant_빈_카테고리_HTTP400() throws Exception {
            String body = """
                    {"passedCategories": []}
                    """;

            mockMvc.perform(post("/api/v1/seasons/{seasonId}/participants", SEASON_ID)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Nickname", "테스트유저")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("요청 바디가 없으면 HTTP 400을 반환한다")
        void registerParticipant_바디_없음_HTTP400() throws Exception {
            mockMvc.perform(post("/api/v1/seasons/{seasonId}/participants", SEASON_ID)
                            .header("X-User-Id", USER_ID.toString())
                            .header("X-User-Nickname", "테스트유저")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/seasons/{seasonId}/me")
    class GetMyParticipation {

        @Test
        @DisplayName("HTTP 200, 내 시즌 참여 정보가 반환된다")
        void getMyParticipation_HTTP200() throws Exception {
            Season season = stubSeason(SEASON_ID, 1, SeasonStatus.ACTIVE);
            SeasonParticipant participant = stubParticipant(season);
            given(seasonService.getMyParticipation(SEASON_ID, USER_ID))
                    .willReturn(participant);

            mockMvc.perform(get("/api/v1/seasons/{seasonId}/me", SEASON_ID)
                            .header("X-User-Id", USER_ID.toString()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("응답 필드 확인 - seasonParticipantId, seasonId, userId, 시드머니 정보")
        void getMyParticipation_응답_필드_확인() throws Exception {
            Season season = stubSeason(SEASON_ID, 1, SeasonStatus.ACTIVE);
            SeasonParticipant participant = stubParticipant(season);
            given(seasonService.getMyParticipation(SEASON_ID, USER_ID))
                    .willReturn(participant);

            mockMvc.perform(get("/api/v1/seasons/{seasonId}/me", SEASON_ID)
                            .header("X-User-Id", USER_ID.toString()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.seasonParticipantId")
                            .value(PARTICIPANT_ID.toString()))
                    .andExpect(jsonPath("$.data.seasonId").value(SEASON_ID.toString()))
                    .andExpect(jsonPath("$.data.userId").value(USER_ID.toString()))
                    .andExpect(jsonPath("$.data.passedCategories[0]").value("STOCK"))
                    .andExpect(jsonPath("$.data.passedCategories[1]").value("ETF"))
                    .andExpect(jsonPath("$.data.baseSeedMoney").value(1000))
                    .andExpect(jsonPath("$.data.totalSeedMoney").value(1000));
        }

        @Test
        @DisplayName("참여 정보가 없으면 HTTP 404를 반환한다")
        void getMyParticipation_없을때_HTTP404() throws Exception {
            given(seasonService.getMyParticipation(SEASON_ID, USER_ID))
                    .willThrow(new NotFoundException("해당 시즌 참여 정보를 찾을 수 없습니다."));

            mockMvc.perform(get("/api/v1/seasons/{seasonId}/me", SEASON_ID)
                            .header("X-User-Id", USER_ID.toString()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
        }
    }
}
