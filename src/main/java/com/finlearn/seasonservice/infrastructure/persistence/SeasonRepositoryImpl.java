package com.finlearn.seasonservice.infrastructure.persistence;

import com.finlearn.seasonservice.domain.Season;
import com.finlearn.seasonservice.domain.repository.SeasonRepository;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SeasonRepositoryImpl implements SeasonRepository {

    private final SeasonJpaRepository seasonJpaRepository;

    @Override
    public Optional<Season> findById(UUID seasonId) {
        return seasonJpaRepository.findById(seasonId);
    }

    @Override
    public List<Season> findAll() {
        return seasonJpaRepository.findAll();
    }

    @Override
    public Optional<Season> findByStatus(SeasonStatus status) {
        // findByStatus 대신 findFirstByStatus 사용 — 동일 status 다중 존재 시 예외 방지
        return seasonJpaRepository.findFirstByStatusOrderBySeasonNumberAsc(status);
    }

    @Override
    public boolean existsBySeasonNumber(Integer seasonNumber) {
        return seasonJpaRepository.existsBySeasonNumber(seasonNumber);
    }

    @Override
    public List<Season> findAllUpcomingToStart(LocalDate today) {
        return seasonJpaRepository.findAllByStatusAndStartDateLessThanEqual(SeasonStatus.UPCOMING, today);
    }

    @Override
    public List<Season> findAllActiveToEnd(LocalDate today) {
        return seasonJpaRepository.findAllByStatusAndEndDateBefore(SeasonStatus.ACTIVE, today);
    }

    @Override
    public boolean existsByOverlappingDates(LocalDate startDate, LocalDate endDate) {
        return seasonJpaRepository.existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(endDate, startDate);
    }

    @Override
    public Season save(Season season) {
        return seasonJpaRepository.save(season);
    }
}
