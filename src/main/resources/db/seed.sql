-- ============================================================
-- seasondb 시드 데이터 — API 테스트용
-- ON CONFLICT DO NOTHING: 재실행해도 중복 에러 없음
--
-- 실행 방법
--   psql -U postgres -d seasondb -f seed.sql
--
-- 포함 데이터
--   seasons         : ENDED(1) / ACTIVE(2) / UPCOMING(3)
--   season_participants : 시즌 2에 테스트 유저 2명
-- ============================================================

-- ──────────────────────────────────────────────────────────────
-- 시즌 데이터
-- ──────────────────────────────────────────────────────────────
INSERT INTO seasons (season_id, season_number, start_date, end_date, status, created_at, updated_at)
VALUES
    -- 시즌 1: 종료됨
    ('00000000-0000-0000-0000-000000000001',
     1,
     '2025-01-01',
     '2025-06-30',
     'ENDED',
     NOW(), NOW()),

    -- 시즌 2: 현재 진행 중 (GET /api/v1/seasons/current 가 반환)
    ('00000000-0000-0000-0000-000000000002',
     2,
     '2025-07-01',
     '2025-12-31',
     'ACTIVE',
     NOW(), NOW()),

    -- 시즌 3: 예정
    ('00000000-0000-0000-0000-000000000003',
     3,
     '2026-01-01',
     '2026-06-30',
     'UPCOMING',
     NOW(), NOW())

ON CONFLICT (season_id) DO NOTHING;


-- ──────────────────────────────────────────────────────────────
-- 시즌 참여자 데이터 (시즌 2 참여자)
-- GET /api/v1/seasons/{seasonId}/me 테스트 시
--   X-User-Id: 00000000-0000-0000-0001-000000000001  (STOCK+ETF 통과)
--   X-User-Id: 00000000-0000-0000-0001-000000000002  (STOCK만 통과)
-- ──────────────────────────────────────────────────────────────
INSERT INTO season_participants (
    season_participant_id,
    season_id,
    user_id,
    user_nickname,
    passed_categories,
    base_seed_money,
    achievement_bonus,
    ranking_bonus,
    total_seed_money,
    paid_at,
    created_at,
    updated_at
)
VALUES
    -- 테스트 유저 1: STOCK + ETF 통과 → 기본 시드 1000
    ('00000000-0000-0000-0002-000000000001',
     '00000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0001-000000000001',
     '테스트유저1',
     'STOCK,ETF',
     1000, 0, 0, 1000,
     NOW(), NOW(), NOW()),

    -- 테스트 유저 2: STOCK만 통과 → 기본 시드 500
    ('00000000-0000-0000-0002-000000000002',
     '00000000-0000-0000-0000-000000000002',
     '00000000-0000-0000-0001-000000000002',
     '테스트유저2',
     'STOCK',
     500, 0, 0, 500,
     NOW(), NOW(), NOW())

ON CONFLICT (season_id, user_id) DO NOTHING;
