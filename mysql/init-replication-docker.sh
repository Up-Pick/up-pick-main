#!/bin/bash

echo "=== MySQL Replication 초기화 시작 ==="

# Replication User 생성
REPL_USER="replicator"
REPL_PASS="replicator_password"

echo "1. Master에서 replication 사용자 생성..."
mysql -h auction-mysql -u root -prootpassword <<EOF
CREATE USER IF NOT EXISTS '${REPL_USER}'@'%' IDENTIFIED WITH mysql_native_password BY '${REPL_PASS}';
GRANT REPLICATION SLAVE ON *.* TO '${REPL_USER}'@'%';
FLUSH PRIVILEGES;
EOF

echo "2. Master의 바이너리 로그 위치 확인..."
MASTER_STATUS=$(mysql -h auction-mysql -u root -prootpassword -e "SHOW MASTER STATUS\G")
echo "$MASTER_STATUS"

BINLOG_FILE=$(echo "$MASTER_STATUS" | grep "File:" | awk '{print $2}')
BINLOG_POS=$(echo "$MASTER_STATUS" | grep "Position:" | awk '{print $2}')

echo "Binary Log File: $BINLOG_FILE"
echo "Binary Log Position: $BINLOG_POS"

if [ -z "$BINLOG_FILE" ] || [ -z "$BINLOG_POS" ]; then
    echo "ERROR: Master 상태를 가져올 수 없습니다!"
    exit 1
fi

echo "3. Slave에서 replication 설정..."
mysql -h localhost -u root -prootpassword <<EOF
STOP SLAVE;
CHANGE MASTER TO
  MASTER_HOST='auction-mysql',
  MASTER_USER='${REPL_USER}',
  MASTER_PASSWORD='${REPL_PASS}',
  MASTER_LOG_FILE='${BINLOG_FILE}',
  MASTER_LOG_POS=${BINLOG_POS};
START SLAVE;
EOF

echo "4. Slave 상태 확인..."
mysql -h localhost -u root -prootpassword -e "SHOW SLAVE STATUS\G"

echo "=== MySQL Replication 초기화 완료 ==="
