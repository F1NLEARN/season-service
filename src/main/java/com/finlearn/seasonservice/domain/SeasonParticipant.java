package com.finlearn.seasonservice.domain;

import com.finlearn.common.domain.BaseEntity;
import com.finlearn.seasonservice.domain.vo.PassedCategory;
import com.finlearn.seasonservice.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Entity
@Table(
        name = "season_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_season_participant_season_user",
                columnNames = {"season_id", "user_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SeasonParticipant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "season_participant_id", updatable = false, nullable = false)
    private UUID seasonParticipantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @Embedded
    private UserId userId;

    @Column(name = "user_nickname", nullable = false, length = 50)
    private String userNickname;

    @Column(name = "passed_categories", nullable = false, length = 100)
    private String passedCategories;

    @Column(name = "base_seed_money", nullable = false)
    private Integer baseSeedMoney;

    @Column(name = "achievement_bonus", nullable = false)
    private Integer achievementBonus;

    @Column(name = "ranking_bonus", nullable = false)
    private Integer rankingBonus;

    @Column(name = "total_seed_money", nullable = false)
    private Integer totalSeedMoney;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Builder
    private SeasonParticipant(Season season, UserId userId, String userNickname,
                              List<PassedCategory> passedCategories,
                              Integer achievementBonus, Integer rankingBonus) {
        this.season = season;
        this.userId = userId;
        this.userNickname = userNickname;
        this.passedCategories = serializeCategories(passedCategories);
        this.baseSeedMoney = calculateBaseSeedMoney(passedCategories);
        this.achievementBonus = achievementBonus;
        this.rankingBonus = rankingBonus;
        this.totalSeedMoney = this.baseSeedMoney + achievementBonus + rankingBonus;
        this.paidAt = LocalDateTime.now();
    }

    public static SeasonParticipant create(Season season, UUID userId, String userNickname,
                                           List<PassedCategory> passedCategories,
                                           int achievementBonus, int rankingBonus) {
        return SeasonParticipant.builder()
                .season(season)
                .userId(UserId.of(userId))
                .userNickname(userNickname)
                .passedCategories(passedCategories)
                .achievementBonus(achievementBonus)
                .rankingBonus(rankingBonus)
                .build();
    }

    public void updateNickname(String newNickname) {
        this.userNickname = newNickname;
    }

    public void applyBonuses(int achievementBonus, int rankingBonus) {
        this.achievementBonus = achievementBonus;
        this.rankingBonus = rankingBonus;
        this.totalSeedMoney = this.baseSeedMoney + achievementBonus + rankingBonus;
        this.paidAt = LocalDateTime.now();
    }

    public List<PassedCategory> getParsedCategories() {
        return Arrays.stream(passedCategories.split(","))
                .map(PassedCategory::valueOf)
                .collect(Collectors.toList());
    }

    private static int calculateBaseSeedMoney(List<PassedCategory> passedCategories) {
        return passedCategories.size() * 500;
    }

    private static String serializeCategories(List<PassedCategory> categories) {
        return categories.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }
}
