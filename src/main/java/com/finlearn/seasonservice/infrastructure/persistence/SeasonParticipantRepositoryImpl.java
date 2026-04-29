package com.finlearn.seasonservice.infrastructure.persistence;

import com.finlearn.seasonservice.domain.SeasonParticipant;
import com.finlearn.seasonservice.domain.repository.SeasonParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SeasonParticipantRepositoryImpl implements SeasonParticipantRepository {

    private final SeasonParticipantJpaRepository participantJpaRepository;

    @Override
    public Optional<SeasonParticipant> findBySeasonIdAndUserId(UUID seasonId, UUID userId) {
        return participantJpaRepository.findBySeasonSeasonIdAndUserId_Value(seasonId, userId);
    }

    @Override
    public List<SeasonParticipant> findAllBySeasonId(UUID seasonId) {
        return participantJpaRepository.findAllBySeasonSeasonId(seasonId);
    }

    @Override
    public boolean existsBySeasonIdAndUserId(UUID seasonId, UUID userId) {
        return participantJpaRepository.existsBySeasonSeasonIdAndUserId_Value(seasonId, userId);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return participantJpaRepository.existsByUserId_Value(userId);
    }

    @Override
    public List<SeasonParticipant> findAllByUserId(UUID userId) {
        return participantJpaRepository.findAllByUserId_Value(userId);
    }

    @Override
    public SeasonParticipant save(SeasonParticipant participant) {
        return participantJpaRepository.save(participant);
    }

    @Override
    public List<SeasonParticipant> saveAll(List<SeasonParticipant> participants) {
        return participantJpaRepository.saveAll(participants);
    }
}
