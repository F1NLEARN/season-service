package com.finlearn.seasonservice.domain.repository;

import com.finlearn.seasonservice.domain.Season;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeasonRepository {

    Optional<Season> findById(UUID seasonId);

    List<Season> findAll();

    Optional<Season> findByStatus(SeasonStatus status);

    boolean existsBySeasonNumber(Integer seasonNumber);

    // UPCOMING 중 시작일이 today 이하인 시즌: 시작 전환 대상
    List<Season> findAllUpcomingToStart(LocalDate today);

    // ACTIVE 중 종료일이 today 이전인 시즌: 종료 전환 대상
    List<Season> findAllActiveToEnd(LocalDate today);

    boolean existsByOverlappingDates(LocalDate startDate, LocalDate endDate);

    Season save(Season season);
}
