package com.finlearn.seasonservice.domain;

import com.finlearn.seasonservice.domain.vo.PassedCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SeasonParticipant 도메인")
class SeasonParticipantTest {

    private Season activeSeason;
    private final UUID USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        activeSeason = Season.create(1, LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31));
        activeSeason.start();
    }

    // ────────────────────────────────────────────────────────────────
    // 기본 시드머니 산정 (카테고리 수 × 500)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("기본 시드머니 산정")
    class BaseSeedMoney {

        @Test
        @DisplayName("카테고리 1개(STOCK) → 기본 시드머니 500")
        void baseSeedMoney_카테고리_1개() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK), 0, 0
            );

            assertThat(participant.getBaseSeedMoney()).isEqualTo(500);
        }

        @Test
        @DisplayName("카테고리 1개(ETF) → 기본 시드머니 500")
        void baseSeedMoney_ETF_1개() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.ETF), 0, 0
            );

            assertThat(participant.getBaseSeedMoney()).isEqualTo(500);
        }

        @Test
        @DisplayName("카테고리 2개(STOCK, ETF) → 기본 시드머니 1000")
        void baseSeedMoney_카테고리_2개() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK, PassedCategory.ETF), 0, 0
            );

            assertThat(participant.getBaseSeedMoney()).isEqualTo(1000);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // 총 시드머니 산정 (기본 + 업적 보너스 + 랭킹 보너스)
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("총 시드머니 산정")
    class TotalSeedMoney {

        @Test
        @DisplayName("보너스 없음 → totalSeedMoney = baseSeedMoney")
        void totalSeedMoney_보너스_없음() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK, PassedCategory.ETF), 0, 0
            );

            assertThat(participant.getTotalSeedMoney()).isEqualTo(1000);
        }

        @Test
        @DisplayName("업적 보너스 200, 랭킹 보너스 100 → totalSeedMoney = baseSeedMoney + 300")
        void totalSeedMoney_보너스_포함() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK, PassedCategory.ETF), 200, 100
            );

            assertThat(participant.getBaseSeedMoney()).isEqualTo(1000);
            assertThat(participant.getAchievementBonus()).isEqualTo(200);
            assertThat(participant.getRankingBonus()).isEqualTo(100);
            assertThat(participant.getTotalSeedMoney()).isEqualTo(1300);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // applyBonuses()
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("applyBonuses()")
    class ApplyBonuses {

        @Test
        @DisplayName("보너스 적용 시 totalSeedMoney가 재계산된다")
        void applyBonuses_totalSeedMoney_재계산() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK), 0, 0
            );

            participant.applyBonuses(500, 200);

            assertThat(participant.getAchievementBonus()).isEqualTo(500);
            assertThat(participant.getRankingBonus()).isEqualTo(200);
            assertThat(participant.getTotalSeedMoney()).isEqualTo(500 + 500 + 200); // base + achievement + ranking
        }

        @Test
        @DisplayName("보너스 재적용 시 이전 보너스 값을 덮어쓴다")
        void applyBonuses_재적용_덮어쓰기() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK), 100, 50
            );

            participant.applyBonuses(300, 200);

            assertThat(participant.getAchievementBonus()).isEqualTo(300);
            assertThat(participant.getRankingBonus()).isEqualTo(200);
            assertThat(participant.getTotalSeedMoney()).isEqualTo(500 + 300 + 200);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // getParsedCategories()
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("getParsedCategories()")
    class GetParsedCategories {

        @Test
        @DisplayName("STOCK만 저장하면 STOCK만 반환된다")
        void getParsedCategories_STOCK() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK), 0, 0
            );

            assertThat(participant.getParsedCategories())
                    .containsExactly(PassedCategory.STOCK);
        }

        @Test
        @DisplayName("STOCK, ETF 저장 시 순서대로 반환된다")
        void getParsedCategories_STOCK_ETF() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK, PassedCategory.ETF), 0, 0
            );

            assertThat(participant.getParsedCategories())
                    .containsExactly(PassedCategory.STOCK, PassedCategory.ETF);
        }
    }

    // ────────────────────────────────────────────────────────────────
    // updateNickname()
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("updateNickname()")
    class UpdateNickname {

        @Test
        @DisplayName("닉네임이 새 값으로 변경된다")
        void updateNickname_성공() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "기존닉네임",
                    List.of(PassedCategory.STOCK), 0, 0
            );

            participant.updateNickname("새닉네임");

            assertThat(participant.getUserNickname()).isEqualTo("새닉네임");
        }
    }

    // ────────────────────────────────────────────────────────────────
    // UserId VO
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("UserId VO")
    class UserIdVo {

        @Test
        @DisplayName("userId.getValue()가 전달된 UUID와 일치한다")
        void userId_VO_값_일치() {
            SeasonParticipant participant = SeasonParticipant.create(
                    activeSeason, USER_ID, "테스트유저",
                    List.of(PassedCategory.STOCK), 0, 0
            );

            assertThat(participant.getUserId().getValue()).isEqualTo(USER_ID);
        }
    }
}
