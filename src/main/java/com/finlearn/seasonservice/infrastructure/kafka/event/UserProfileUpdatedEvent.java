package com.finlearn.seasonservice.infrastructure.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.UUID;

/**
 * 유저 서비스로부터 수신
 * 닉네임 또는 프로필 이미지 변경 시 발행
 * 수신 후: season_participants의 user_nickname VO 스냅샷 갱신
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdatedEvent {

    private UUID userId;
    private String nickname;
    private String profileImage;
}
