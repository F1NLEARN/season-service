package com.finlearn.seasonservice.domain;

import com.finlearn.common.exception.BadRequestException;
import com.finlearn.common.exception.ConflictException;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Season 도메인")
class SeasonTest {

    private static final LocalDate START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate END_DATE   = LocalDate.of(2025, 1, 31);

    // ────────────────────────────────────────────────────────────────
    // create()
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("정상 생성 시 상태는 UPCOMING이다")
        void create_성공_UPCOMING_상태() {
            Season season = Season.create(1, START_DATE, END_DATE);

            assertThat(season.getSeasonNumber()).isEqualTo(1);
            assertThat(season.getStartDate()).isEqualTo(START_DATE);
            assertThat(season.getEndDate()).isEqualTo(END_DATE);
            assertThat(season.getStatus()).isEqualTo(SeasonStatus.UPCOMING);
        }

        @Test
        @DisplayName("종료일이 시작일과 같으면 BadRequestException")
        void create_실패_종료일_같음() {
            assertThatThrownBy(() -> Season.create(1, START_DATE, START_DATE))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("종료일은 시작일보다 이후여야 합니다.");
        }

        @Test
        @DisplayName("종료일이 시작일보다 이전이면 BadRequestException")
        void create_실패_종료일_이전() {
            LocalDate beforeStart = START_DATE.minusDays(1);

            assertThatThrownBy(() -> Season.create(1, START_DATE, beforeStart))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("종료일은 시작일보다 이후여야 합니다.");
        }
    }

    // ────────────────────────────────────────────────────────────────
    // start()
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("start()")
    class Start {

        @Test
        @DisplayName("UPCOMING 상태에서 start() 호출 시 ACTIVE로 전환된다")
        void start_성공_UPCOMING_to_ACTIVE() {
            Season season = Season.create(1, START_DATE, END_DATE);

            season.start();

            assertThat(season.getStatus()).isEqualTo(SeasonStatus.ACTIVE);
        }

        @Test
        @DisplayName("ACTIVE 상태에서 start() 호출 시 ConflictException")
        void start_실패_이미_ACTIVE() {
            Season season = Season.create(1, START_DATE, END_DATE);
            season.start();

            assertThatThrownBy(season::start)
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("UPCOMING 상태의 시즌만 시작할 수 있습니다.");
        }

        @Test
        @DisplayName("ENDED 상태에서 start() 호출 시 ConflictException")
        void start_실패_이미_ENDED() {
            Season season = Season.create(1, START_DATE, END_DATE);
            season.start();
            season.end();

            assertThatThrownBy(season::start)
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("UPCOMING 상태의 시즌만 시작할 수 있습니다.");
        }
    }

    // ────────────────────────────────────────────────────────────────
    // end()
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("end()")
    class End {

        @Test
        @DisplayName("ACTIVE 상태에서 end() 호출 시 ENDED로 전환된다")
        void end_성공_ACTIVE_to_ENDED() {
            Season season = Season.create(1, START_DATE, END_DATE);
            season.start();

            season.end();

            assertThat(season.getStatus()).isEqualTo(SeasonStatus.ENDED);
        }

        @Test
        @DisplayName("UPCOMING 상태에서 end() 호출 시 ConflictException")
        void end_실패_UPCOMING_상태() {
            Season season = Season.create(1, START_DATE, END_DATE);

            assertThatThrownBy(season::end)
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("ACTIVE 상태의 시즌만 종료할 수 있습니다.");
        }

        @Test
        @DisplayName("ENDED 상태에서 end() 호출 시 ConflictException")
        void end_실패_이미_ENDED() {
            Season season = Season.create(1, START_DATE, END_DATE);
            season.start();
            season.end();

            assertThatThrownBy(season::end)
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("ACTIVE 상태의 시즌만 종료할 수 있습니다.");
        }
    }

    // ────────────────────────────────────────────────────────────────
    // canStart() / canEnd()
    // ────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("canStart()")
    class CanStart {

        @Test
        @DisplayName("UPCOMING 상태이고 today가 startDate와 같으면 true")
        void canStart_true_시작일_당일() {
            Season season = Season.create(1, START_DATE, END_DATE);

            assertThat(season.canStart(START_DATE)).isTrue();
        }

        @Test
        @DisplayName("UPCOMING 상태이고 today가 startDate 이후면 true")
        void canStart_true_시작일_이후() {
            Season season = Season.create(1, START_DATE, END_DATE);

            assertThat(season.canStart(START_DATE.plusDays(1))).isTrue();
        }

        @Test
        @DisplayName("UPCOMING 상태이지만 today가 startDate 이전이면 false")
        void canStart_false_시작일_이전() {
            Season season = Season.create(1, START_DATE, END_DATE);

            assertThat(season.canStart(START_DATE.minusDays(1))).isFalse();
        }

        @Test
        @DisplayName("ACTIVE 상태이면 false")
        void canStart_false_ACTIVE_상태() {
            Season season = Season.create(1, START_DATE, END_DATE);
            season.start();

            assertThat(season.canStart(START_DATE)).isFalse();
        }
    }

    @Nested
    @DisplayName("canEnd()")
    class CanEnd {

        @Test
        @DisplayName("ACTIVE 상태이고 today가 endDate 이후면 true")
        void canEnd_true_종료일_이후() {
            Season season = Season.create(1, START_DATE, END_DATE);
            season.start();

            assertThat(season.canEnd(END_DATE.plusDays(1))).isTrue();
        }

        @Test
        @DisplayName("ACTIVE 상태이지만 today가 endDate와 같으면 false")
        void canEnd_false_종료일_당일() {
            Season season = Season.create(1, START_DATE, END_DATE);
            season.start();

            assertThat(season.canEnd(END_DATE)).isFalse();
        }

        @Test
        @DisplayName("UPCOMING 상태이면 false")
        void canEnd_false_UPCOMING_상태() {
            Season season = Season.create(1, START_DATE, END_DATE);

            assertThat(season.canEnd(END_DATE.plusDays(1))).isFalse();
        }
    }
}
