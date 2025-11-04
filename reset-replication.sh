#!/bin/bash

echo "=== MySQL Replication 재설정 스크립트 ==="
echo ""
echo "이 스크립트는 다음 경우에 사용합니다:"
echo "  - Replication이 제대로 설정되지 않았을 때"
echo "  - Slave 상태가 비정상일 때"
echo "  - Volume을 삭제하고 다시 시작할 때"
echo ""

# 컨테이너 이름 확인
SLAVE_CONTAINER=$(docker ps --filter "name=auction-mysql-slave" --format "{{.Names}}" | head -n 1)

if [ -z "$SLAVE_CONTAINER" ]; then
    echo "❌ auction-mysql-slave 컨테이너를 찾을 수 없습니다."
    echo "docker-compose up -d로 컨테이너를 먼저 실행하세요."
    exit 1
fi

echo "✅ Slave 컨테이너 발견: $SLAVE_CONTAINER"
echo ""

# 스크립트 복사 및 실행 (Windows Git Bash 호환)
echo "📋 Replication 초기화 스크립트를 컨테이너에 복사 중..."
MSYS_NO_PATHCONV=1 docker cp mysql/init-replication-docker.sh $SLAVE_CONTAINER:/tmp/init-replication.sh

echo "🚀 Replication 설정 실행 중..."
MSYS_NO_PATHCONV=1 docker exec $SLAVE_CONTAINER bash /tmp/init-replication.sh

echo ""
echo "✅ 완료! Slave 상태 확인:"
MSYS_NO_PATHCONV=1 docker exec $SLAVE_CONTAINER mysql -uroot -prootpassword -e "SHOW SLAVE STATUS\G" | grep -E "(Slave_IO_Running|Slave_SQL_Running|Seconds_Behind_Master)"
