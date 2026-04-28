package com.finlearn.seasonservice.domain.repository;

import com.finlearn.seasonservice.domain.SeasonParticipant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeasonParticipantRepository {

    Optional<SeasonParticipant> findBySeasonIdAndUserId(UUID seasonId, UUID userId);

    List<SeasonParticipant> findAllBySeasonId(UUID seasonId);

    boolean existsBySeasonIdAndUserId(UUID seasonId, UUID userId);

    boolean existsByUserId(UUID userId);

    List<SeasonParticipant> findAllByUserId(UUID userId);

    SeasonParticipant save(SeasonParticipant participant);

    List<SeasonParticipant> saveAll(List<SeasonParticipant> participants);
}
