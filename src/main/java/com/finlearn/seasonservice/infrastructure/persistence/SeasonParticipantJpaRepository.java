package com.finlearn.seasonservice.infrastructure.persistence;

import com.finlearn.seasonservice.domain.SeasonParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeasonParticipantJpaRepository extends JpaRepository<SeasonParticipant, UUID> {

    Optional<SeasonParticipant> findBySeasonSeasonIdAndUserId_Value(UUID seasonId, UUID userId);

    List<SeasonParticipant> findAllBySeasonSeasonId(UUID seasonId);

    boolean existsBySeasonSeasonIdAndUserId_Value(UUID seasonId, UUID userId);

    // 유저가 이전 시즌에 참여한 이력이 있는지 확인
    boolean existsByUserId_Value(UUID userId);

    // 닉네임 스냅샷 일괄 갱신을 위해 특정 유저의 전체 참여 기록 조회
    List<SeasonParticipant> findAllByUserId_Value(UUID userId);
}
