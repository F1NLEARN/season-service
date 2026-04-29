package com.finlearn.seasonservice.infrastructure.persistence;

import com.finlearn.seasonservice.domain.Season;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeasonJpaRepository extends JpaRepository<Season, UUID> {

    Optional<Season> findByStatus(SeasonStatus status);

    List<Season> findAllByStatusOrderBySeasonNumberDesc(SeasonStatus status);

    boolean existsBySeasonNumber(Integer seasonNumber);

    // start_date <= today AND status: UPCOMING → 시작 대상
    List<Season> findAllByStatusAndStartDateLessThanEqual(SeasonStatus status, LocalDate date);

    // end_date < today AND status: ACTIVE → 종료 대상
    List<Season> findAllByStatusAndEndDateBefore(SeasonStatus status, LocalDate date);

    // 기간 겹침 검증: (startDate <= endDate) AND (endDate >= startDate)
    boolean existsByStartDateLessThanEqualAndEndDateGreaterThanEqual(LocalDate endDate, LocalDate startDate);
}
