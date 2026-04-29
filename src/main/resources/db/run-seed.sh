#!/bin/bash
# ============================================================
# seasondb 시드 데이터 삽입 스크립트
# 사용: ./run-seed.sh [host] [port] [user]
#
# 기본값: localhost 5432 postgres
# ============================================================

HOST=${1:-localhost}
PORT=${2:-5432}
USER=${3:-postgres}

echo "▶ seasondb에 시드 데이터 삽입 중... (${USER}@${HOST}:${PORT})"

psql -h "$HOST" -p "$PORT" -U "$USER" -d seasondb -f "$(dirname "$0")/seed.sql"

if [ $? -eq 0 ]; then
    echo "✅ 시드 데이터 삽입 완료"
else
    echo "❌ 오류 발생 — psql이 설치되어 있는지 확인하세요"
fi
