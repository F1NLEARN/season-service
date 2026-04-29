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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonParticipantRepository participantRepository;

    // ────────────────────────────────────────────────────────────────
    // 시즌 조회
    // ────────────────────────────────────────────────────────────────

    public List<Season> getAllSeasons() {
        return seasonRepository.findAll();
    }

    public Season getCurrentSeason() {
        return seasonRepository.findByStatus(SeasonStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException("현재 진행 중인 시즌이 없습니다."));
    }

    public SeasonParticipant getMyParticipation(UUID seasonId, UUID userId) {
        return participantRepository.findBySeasonIdAndUserId(seasonId, userId)
                .orElseThrow(() -> new NotFoundException("해당 시즌 참여 정보를 찾을 수 없습니다."));
    }

    // ────────────────────────────────────────────────────────────────
    // 시즌 참여 등록
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public SeasonParticipant registerParticipant(UUID seasonId, UUID userId,
                                                 String userNickname,
                                                 List<PassedCategory> passedCategories) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new NotFoundException("시즌을 찾을 수 없습니다."));

        if (season.getStatus() != SeasonStatus.ACTIVE) {
            throw new BadRequestException("진행 중인 시즌에만 참여할 수 있습니다.");
        }

        if (participantRepository.existsBySeasonIdAndUserId(seasonId, userId)) {
            throw new ConflictException("이미 해당 시즌에 참여 중입니다.");
        }

        // 신규 참여: 업적 및 랭킹 보너스 없음 (RankingFinalized 이벤트 수신 시 갱신)
        SeasonParticipant participant = SeasonParticipant.create(
                season, userId, userNickname, passedCategories, 0, 0
        );
        participantRepository.save(participant);

        log.info("[SeasonService] 시즌 참여 등록 완료: seasonId={}, userId={}, totalSeedMoney={}",
                seasonId, userId, participant.getTotalSeedMoney());

        return participant;
    }
}