-- ============================================================
-- seasondb 스키마
-- Hibernate ddl-auto: update 가 자동 생성하므로 직접 실행 불필요
-- 테이블을 초기화하거나 수동으로 재생성할 때 사용
-- ============================================================

-- 연결 대상: seasondb
-- \c seasondb

-- ──────────────────────────────────────────────────────────────
-- 테이블 삭제 (초기화용)
-- ──────────────────────────────────────────────────────────────
DROP TABLE IF EXISTS season_participants CASCADE;
DROP TABLE IF EXISTS seasons CASCADE;

-- ──────────────────────────────────────────────────────────────
-- seasons
-- ──────────────────────────────────────────────────────────────
CREATE TABLE seasons (
    season_id        UUID         PRIMARY KEY,
    season_number    INT          NOT NULL UNIQUE,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    status           VARCHAR(20)  NOT NULL CHECK (status IN ('UPCOMING', 'ACTIVE', 'ENDED')),
    created_at       TIMESTAMP,
    created_by       UUID,
    updated_at       TIMESTAMP,
    updated_by       UUID,
    deleted_at       TIMESTAMP,
    deleted_by       UUID
);

-- ──────────────────────────────────────────────────────────────
-- season_participants
-- ──────────────────────────────────────────────────────────────
CREATE TABLE season_participants (
    season_participant_id UUID        PRIMARY KEY,
    season_id             UUID        NOT NULL REFERENCES seasons(season_id),
    user_id               UUID        NOT NULL,
    user_nickname         VARCHAR(50) NOT NULL,
    passed_categories     VARCHAR(100) NOT NULL,
    base_seed_money       INT         NOT NULL DEFAULT 0,
    achievement_bonus     INT         NOT NULL DEFAULT 0,
    ranking_bonus         INT         NOT NULL DEFAULT 0,
    total_seed_money      INT         NOT NULL DEFAULT 0,
    paid_at               TIMESTAMP,
    created_at            TIMESTAMP,
    created_by            UUID,
    updated_at            TIMESTAMP,
    updated_by            UUID,
    deleted_at            TIMESTAMP,
    deleted_by            UUID,

    CONSTRAINT uk_season_participant_season_user UNIQUE (season_id, user_id)
);
