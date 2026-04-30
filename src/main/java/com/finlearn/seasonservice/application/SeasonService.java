package com.finlearn.seasonservice.application;

import com.finlearn.common.exception.BadRequestException;
import com.finlearn.common.exception.ConflictException;
import com.finlearn.common.exception.NotFoundException;
import com.finlearn.seasonservice.domain.Season;
import com.finlearn.seasonservice.domain.SeasonParticipant;
import com.finlearn.seasonservice.domain.event.SeasonEndedEvent;
import com.finlearn.seasonservice.domain.event.SeasonStartedEvent;
import com.finlearn.seasonservice.domain.repository.SeasonParticipantRepository;
import com.finlearn.seasonservice.domain.repository.SeasonRepository;
import com.finlearn.seasonservice.domain.vo.PassedCategory;
import com.finlearn.seasonservice.domain.vo.SeasonStatus;
import com.finlearn.seasonservice.infrastructure.client.AchievementClient;
import com.finlearn.seasonservice.infrastructure.client.RankingClient;
import com.finlearn.seasonservice.infrastructure.kafka.SeasonEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeasonService {

    private final SeasonRepository seasonRepository;
    private final SeasonParticipantRepository participantRepository;
    private final SeasonEventProducer seasonEventProducer;
    private final AchievementClient achievementClient;
    private final RankingClient rankingClient;

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

        // 신규 참여: 업적/랭킹 보너스 없음 (RankingFinalized 이벤트 수신 시 갱신)
        SeasonParticipant participant = SeasonParticipant.create(
                season, userId, userNickname, passedCategories, 0, 0
        );
        participantRepository.save(participant);

        log.info("[SeasonService] 시즌 참여 등록 완료: seasonId={}, userId={}, totalSeedMoney={}",
                seasonId, userId, participant.getTotalSeedMoney());

        return participant;
    }

    // ────────────────────────────────────────────────────────────────
    // 스케줄러 호출 메서드
    // ────────────────────────────────────────────────────────────────

    @Transactional
    public int startUpcomingSeasons() {
        LocalDate today = LocalDate.now();
        List<Season> targets = seasonRepository.findAllUpcomingToStart(today);

        for (Season season : targets) {
            season.start();
            seasonRepository.save(season);

            seasonEventProducer.publishSeasonStarted(new SeasonStartedEvent(
                    season.getSeasonId(),
                    season.getSeasonNumber(),
                    season.getStartDate(),
                    season.getEndDate()
            ));

            log.info("[SeasonService] 시즌 시작: seasonId={}, seasonNumber={}",
                    season.getSeasonId(), season.getSeasonNumber());
        }
        return targets.size();
    }

    @Transactional
    public int endActiveSeasons() {
        LocalDate today = LocalDate.now();
        List<Season> targets = seasonRepository.findAllActiveToEnd(today);

        for (Season season : targets) {
            season.end();
            seasonRepository.save(season);

            seasonEventProducer.publishSeasonEnded(new SeasonEndedEvent(
                    season.getSeasonId(),
                    season.getSeasonNumber()
            ));

            log.info("[SeasonService] 시즌 종료: seasonId={}, seasonNumber={}",
                    season.getSeasonId(), season.getSeasonNumber());
        }
        return targets.size();
    }

    // ────────────────────────────────────────────────────────────────
    // Kafka 이벤트 처리
    // ────────────────────────────────────────────────────────────────

    /**
     * RankingFinalized 이벤트 수신 후 처리
     * 종료된 시즌 기준으로 다음 시즌 참여자들의 시드머니 보너스를 갱신
     */
    @Transactional
    public void processRankingFinalized(UUID endedSeasonId) {
        log.info("[SeasonService] RankingFinalized 처리 시작: endedSeasonId={}", endedSeasonId);

        Optional<Season> nextSeasonOpt = seasonRepository.findByStatus(SeasonStatus.UPCOMING);
        if (nextSeasonOpt.isEmpty()) {
            log.warn("[SeasonService] UPCOMING 상태의 다음 시즌이 없어 보너스 산정을 건너뜁니다.");
            return;
        }

        Season nextSeason = nextSeasonOpt.get();
        List<SeasonParticipant> nextParticipants =
                participantRepository.findAllBySeasonId(nextSeason.getSeasonId());

        for (SeasonParticipant participant : nextParticipants) {
            UUID userId = participant.getUserId().getValue();

            int achievementBonus = calculateAchievementBonus(userId, endedSeasonId);
            int rankingBonus = calculateRankingBonus(userId, endedSeasonId);

            participant.applyBonuses(achievementBonus, rankingBonus);
            participantRepository.save(participant);

            log.debug("[SeasonService] 시드머니 보너스 적용: userId={}, achievement={}, ranking={}, total={}",
                    userId, achievementBonus, rankingBonus, participant.getTotalSeedMoney());
        }

        log.info("[SeasonService] RankingFinalized 처리 완료: 총 {}명 업데이트", nextParticipants.size());
    }

    /**
     * UserProfileUpdated 이벤트 수신 후 처리
     * season_participants의 user_nickname VO 스냅샷 갱신
     */
    @Transactional
    public void syncUserNickname(UUID userId, String newNickname) {
        List<SeasonParticipant> participants = participantRepository.findAllByUserId(userId);
        participants.forEach(p -> p.updateNickname(newNickname));
        participantRepository.saveAll(participants);
        log.info("[SeasonService] 닉네임 스냅샷 갱신 완료: userId={}, nickname={}, count={}",
                userId, newNickname, participants.size());
    }

    // ────────────────────────────────────────────────────────────────
    // 시드머니 보너스 산정
    // ────────────────────────────────────────────────────────────────

    /**
     * 전 시즌 업적 달성 수 기반 보너스 산정 (단위: 만원)
     * 0개: 0
     * 1~3개: 200
     * 4~7개: 500
     * 8개 이상: 1000
     */    private int calculateAchievementBonus(UUID userId, UUID seasonId) {
        int count = achievementClient.getAchievementCount(userId, seasonId);
        if (count == 0) return 0;
        if (count <= 3) return 200;
        if (count <= 7) return 500;
        return 1000;
    }

    /**
     * 전 시즌 랭킹 순위 기반 보너스 산정 (단위: 만원)
     * 1위: 200
     * 2~5위: 100
     * 6~20위: 50
     * 21위 이상: 0
     */    private int calculateRankingBonus(UUID userId, UUID seasonId) {
        Integer rank = rankingClient.getRank(userId, seasonId);
        if (rank == null) return 0;
        if (rank == 1) return 200;
        if (rank <= 5) return 100;
        if (rank <= 20) return 50;
        return 0;
    }
}