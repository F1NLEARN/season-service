package com.finlearn.seasonservice.application;

import com.finlearn.common.exception.BadRequestException;
import com.finlearn.common.exception.ConflictException;
import com.finlearn.common.exception.NotFoundException;
import com.finlearn.seasonservice.domain.Season;
import com.finlearn.seasonservice.domain.SeasonParticipant;
import com.finlearn.seasonservice.domain.repository.SeasonParticipantRepository;
import com.finlearn.seasonservice.domain.repository.SeasonRepository;
import com.finlearn.seasonservice.domain.vo.PassedCategory;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import com.finlearn.seasonservice.domain.vo.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeasonService")
class SeasonServiceTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private SeasonParticipantRepository participantRepository;

    @InjectMocks
    private SeasonService seasonService;

    private Season mockSeason(SeasonStatus status) {
        Season season = mock(Season.class);
        given(season.getSeasonId()).willReturn(UUID.randomUUID());
        given(season.getSeasonNumber()).willReturn(1);
        given(season.getStartDate()).willReturn(LocalDate.of(2025, 1, 1));
        given(season.getEndDate()).willReturn(LocalDate.of(2025, 1, 31));
        given(season.getStatus()).willReturn(status);
        return season;
    }

    private SeasonParticipant mockParticipant(Season season, UUID userId) {
        SeasonParticipant participant = mock(SeasonParticipant.class);
        given(participant.getSeasonParticipantId()).willReturn(UUID.randomUUID());
        given(participant.getSeason()).willReturn(season);
        given(participant.getUserId()).willReturn(UserId.of(userId));
        given(participant.getUserNickname()).willReturn("테스트유저");
        given(participant.getBaseSeedMoney()).willReturn(1000);
        given(participant.getAchievementBonus()).willReturn(0);
        given(participant.getRankingBonus()).willReturn(0);
        given(participant.getTotalSeedMoney()).willReturn(1000);
        given(participant.getParsedCategories()).willReturn(List.of(PassedCategory.STOCK, PassedCategory.ETF));
        return participant;
    }

    // ────────────────────────────────────────────────────────────────
    // 시즌 조회
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getAllSeasons()")
    class GetAllSeasons {

        @Test
        @DisplayName("전체 시즌 목록을 반환한다")
        void getAllSeasons_전체_시즌_반환() {
            Season season1 = mockSeason(SeasonStatus.ENDED);
            Season season2 = mockSeason(SeasonStatus.ACTIVE);
            given(seasonRepository.findAll()).willReturn(List.of(season1, season2));

            List<Season> result = seasonService.getAllSeasons();

            assertThat(result).hasSize(2);
            assertThat(result).containsExactly(season1, season2);
        }

        @Test
        @DisplayName("시즌이 없으면 빈 리스트를 반환한다")
        void getAllSeasons_빈_리스트_반환() {
            given(seasonRepository.findAll()).willReturn(List.of());

            List<Season> result = seasonService.getAllSeasons();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getCurrentSeason()")
    class GetCurrentSeason {

        @Test
        @DisplayName("ACTIVE 시즌이 있으면 해당 시즌을 반환한다")
        void getCurrentSeason_ACTIVE_시즌_반환() {
            Season activeSeason = mockSeason(SeasonStatus.ACTIVE);
            given(seasonRepository.findByStatus(SeasonStatus.ACTIVE))
                    .willReturn(Optional.of(activeSeason));

            Season result = seasonService.getCurrentSeason();

            assertThat(result.getStatus()).isEqualTo(SeasonStatus.ACTIVE);
            assertThat(result.getSeasonNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("ACTIVE 시즌이 없으면 NotFoundException을 던진다")
        void getCurrentSeason_없을때_NotFoundException() {
            given(seasonRepository.findByStatus(SeasonStatus.ACTIVE))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> seasonService.getCurrentSeason())
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("현재 진행 중인 시즌이 없습니다.");
        }

        @Test
        @DisplayName("반환된 시즌의 필드 값이 일치한다")
        void getCurrentSeason_응답_필드_일치() {
            Season activeSeason = mock(Season.class);
            UUID expectedId = UUID.randomUUID();
            given(activeSeason.getSeasonId()).willReturn(expectedId);
            given(activeSeason.getSeasonNumber()).willReturn(2);
            given(activeSeason.getStartDate()).willReturn(LocalDate.of(2025, 2, 1));
            given(activeSeason.getEndDate()).willReturn(LocalDate.of(2025, 2, 28));
            given(activeSeason.getStatus()).willReturn(SeasonStatus.ACTIVE);
            given(seasonRepository.findByStatus(SeasonStatus.ACTIVE))
                    .willReturn(Optional.of(activeSeason));

            Season result = seasonService.getCurrentSeason();

            assertThat(result.getSeasonId()).isEqualTo(expectedId);
            assertThat(result.getSeasonNumber()).isEqualTo(2);
            assertThat(result.getStartDate()).isEqualTo(LocalDate.of(2025, 2, 1));
            assertThat(result.getEndDate()).isEqualTo(LocalDate.of(2025, 2, 28));
            assertThat(result.getStatus()).isEqualTo(SeasonStatus.ACTIVE);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // 시즌 참여 등록 및 조회
    // ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("registerParticipant()")
    class RegisterParticipant {

        private final UUID SEASON_ID = UUID.randomUUID();
        private final UUID USER_ID   = UUID.randomUUID();

        @Test
        @DisplayName("STOCK + ETF 통과 시 기본 시드머니 1000이 산정된다")
        void registerParticipant_시드머니_정상_계산() {
            Season activeSeason = mockSeason(SeasonStatus.ACTIVE);
            given(seasonRepository.findById(SEASON_ID)).willReturn(Optional.of(activeSeason));
            given(participantRepository.existsBySeasonIdAndUserId(SEASON_ID, USER_ID)).willReturn(false);
            given(participantRepository.save(any(SeasonParticipant.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            SeasonParticipant result = seasonService.registerParticipant(
                    SEASON_ID, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK, PassedCategory.ETF)
            );

            assertThat(result.getBaseSeedMoney()).isEqualTo(1000);   // 2 × 500
            assertThat(result.getTotalSeedMoney()).isEqualTo(1000);  // 보너스 없음
            assertThat(result.getAchievementBonus()).isEqualTo(0);
            assertThat(result.getRankingBonus()).isEqualTo(0);
        }

        @Test
        @DisplayName("STOCK 1개만 통과 시 기본 시드머니 500이 산정된다")
        void registerParticipant_시드머니_500() {
            Season activeSeason = mockSeason(SeasonStatus.ACTIVE);
            given(seasonRepository.findById(SEASON_ID)).willReturn(Optional.of(activeSeason));
            given(participantRepository.existsBySeasonIdAndUserId(SEASON_ID, USER_ID)).willReturn(false);
            given(participantRepository.save(any(SeasonParticipant.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            SeasonParticipant result = seasonService.registerParticipant(
                    SEASON_ID, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK)
            );

            assertThat(result.getBaseSeedMoney()).isEqualTo(500);  // 1 × 500
        }

        @Test
        @DisplayName("정상 참여 등록 시 반환 필드가 일치한다")
        void registerParticipant_응답_필드_일치() {
            Season activeSeason = mockSeason(SeasonStatus.ACTIVE);
            given(seasonRepository.findById(SEASON_ID)).willReturn(Optional.of(activeSeason));
            given(participantRepository.existsBySeasonIdAndUserId(SEASON_ID, USER_ID)).willReturn(false);
            given(participantRepository.save(any(SeasonParticipant.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            SeasonParticipant result = seasonService.registerParticipant(
                    SEASON_ID, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK, PassedCategory.ETF)
            );

            assertThat(result.getUserId().getValue()).isEqualTo(USER_ID);
            assertThat(result.getUserNickname()).isEqualTo("테스트유저");
            assertThat(result.getParsedCategories())
                    .containsExactly(PassedCategory.STOCK, PassedCategory.ETF);
            assertThat(result.getPaidAt()).isNotNull();
        }

        @Test
        @DisplayName("이미 참여 중인 시즌에 재참여 시 ConflictException을 던진다")
        void registerParticipant_중복참여_ConflictException() {
            Season activeSeason = mockSeason(SeasonStatus.ACTIVE);
            given(seasonRepository.findById(SEASON_ID)).willReturn(Optional.of(activeSeason));
            given(participantRepository.existsBySeasonIdAndUserId(SEASON_ID, USER_ID)).willReturn(true);

            assertThatThrownBy(() -> seasonService.registerParticipant(
                    SEASON_ID, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK)
            ))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("이미 해당 시즌에 참여 중입니다.");
        }

        @Test
        @DisplayName("UPCOMING 시즌 참여 시 BadRequestException을 던진다")
        void registerParticipant_UPCOMING_BadRequestException() {
            Season upcomingSeason = mockSeason(SeasonStatus.UPCOMING);
            given(seasonRepository.findById(SEASON_ID)).willReturn(Optional.of(upcomingSeason));

            assertThatThrownBy(() -> seasonService.registerParticipant(
                    SEASON_ID, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK)
            ))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("진행 중인 시즌에만 참여할 수 있습니다.");
        }

        @Test
        @DisplayName("ENDED 시즌 참여 시 BadRequestException을 던진다")
        void registerParticipant_ENDED_BadRequestException() {
            Season endedSeason = mockSeason(SeasonStatus.ENDED);
            given(seasonRepository.findById(SEASON_ID)).willReturn(Optional.of(endedSeason));

            assertThatThrownBy(() -> seasonService.registerParticipant(
                    SEASON_ID, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK)
            ))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("진행 중인 시즌에만 참여할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("getMyParticipation()")
    class GetMyParticipation {

        private final UUID SEASON_ID = UUID.randomUUID();
        private final UUID USER_ID   = UUID.randomUUID();

        @Test
        @DisplayName("참여 정보가 있으면 SeasonParticipant를 반환한다")
        void getMyParticipation_정상_반환() {
            Season season = mockSeason(SeasonStatus.ACTIVE);
            SeasonParticipant participant = mockParticipant(season, USER_ID);
            given(participantRepository.findBySeasonIdAndUserId(SEASON_ID, USER_ID))
                    .willReturn(Optional.of(participant));

            SeasonParticipant result = seasonService.getMyParticipation(SEASON_ID, USER_ID);

            assertThat(result.getUserId().getValue()).isEqualTo(USER_ID);
            assertThat(result.getTotalSeedMoney()).isEqualTo(1000);
        }

        @Test
        @DisplayName("참여 정보가 없으면 NotFoundException을 던진다")
        void getMyParticipation_없을때_NotFoundException() {
            given(participantRepository.findBySeasonIdAndUserId(SEASON_ID, USER_ID))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> seasonService.getMyParticipation(SEASON_ID, USER_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("해당 시즌 참여 정보를 찾을 수 없습니다.");
        }
    }
}