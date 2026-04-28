package com.finlearn.seasonservice.domain;

import com.finlearn.common.domain.BaseEntity;
import com.finlearn.common.exception.BadRequestException;
import com.finlearn.common.exception.ConflictException;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Entity
@Table(name = "seasons")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Season extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "season_id", updatable = false, nullable = false)
    private UUID seasonId;

    @Column(name = "season_number", nullable = false, unique = true)
    private Integer seasonNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SeasonStatus status;

    @Builder
    private Season(Integer seasonNumber, LocalDate startDate, LocalDate endDate) {
        this.seasonNumber = seasonNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = SeasonStatus.UPCOMING;
    }

    public static Season create(Integer seasonNumber, LocalDate startDate, LocalDate endDate) {
        validateDates(startDate, endDate);
        return Season.builder()
                .seasonNumber(seasonNumber)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    public void start() {
        if (this.status != SeasonStatus.UPCOMING) {
            throw new ConflictException("UPCOMING 상태의 시즌만 시작할 수 있습니다.");
        }
        this.status = SeasonStatus.ACTIVE;
    }

    public void end() {
        if (this.status != SeasonStatus.ACTIVE) {
            throw new ConflictException("ACTIVE 상태의 시즌만 종료할 수 있습니다.");
        }
        this.status = SeasonStatus.ENDED;
    }

    public boolean canStart(LocalDate today) {
        return this.status == SeasonStatus.UPCOMING && !this.startDate.isAfter(today);
    }

    public boolean canEnd(LocalDate today) {
        return this.status == SeasonStatus.ACTIVE && this.endDate.isBefore(today);
    }

    private static void validateDates(LocalDate startDate, LocalDate endDate) {
        if (!endDate.isAfter(startDate)) {
            throw new BadRequestException("종료일은 시작일보다 이후여야 합니다.");
        }
    }
}
