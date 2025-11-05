## 목차

- [도커 실행](#도커-실행)
- [도커 종료](#도커-종료)
- [REST Docs](#rest-docs)
- [모니터링 대시보드](#모니터링-대시보드)
- [k6 Load Testing](#k6-load-testing)

## 도커 실행

### windows

docker-compose up --build

### linux

docker compose up --build

## 도커 종료

### windows

docker-compose down

### linux

docker compose down

## REST Docs

### 테스트 + 문서 생성

./gradlew test asciidoctor

### 테스트 문서 생성 과정

- `./gradlew test` 실행 시 REST Docs 스니펫이 `build/generated-snippets` 아래에 떨어집니다.
- 테스트가 끝나면 Gradle이 `generateRestDocsIndex` 작업을 자동으로 수행해, 방금 생성된 모든 스니펫 디렉터리를 읽고 `auto-index.adoc` 파일을 만듭니다.
- `src/docs/asciidoc/index.adoc`은 이 `auto-index.adoc`을 그대로 include 하도록 되어 있어, 새 테스트가 추가돼도 README를 포함한 문서 템플릿을 다시 손댈 필요가
  없습니다.
- 이어서 `asciidoctor` 작업이 실행되어 `build/docs/asciidoc/index.html`이 갱신되며, 여기에는 모든 REST Docs 테스트 결과가 자동으로 반영됩니다.
- 전역 `RestDocsMockMvcConfigurationCustomizer`가 요청/응답을 pretty-print 하며,
  `src/test/resources/org/springframework/restdocs/templates/asciidoctor`에 둔 커스텀 템플릿이 응답 필드 표에 `Notes` 컬럼(필수 여부, 포맷 등)을
  자동으로 붙여 줍니다.

### 새 엔드포인트를 문서화하려면

1. MockMvc 테스트 추가: `WebMvcTest` 기반 테스트 클래스에 `@AutoConfigureRestDocs(outputDir = "build/generated-snippets")`와
   `@Import(RestDocsConfig.class)`를 붙이고, `mockMvc.perform(...)`에 `andDo(document("operation-name", ...))`를 연결합니다. 문서 표에
   노출할 정보는 `fieldWithPath(...).attributes(key("format").value("yyyy-MM-dd"))`처럼 `Attributes`로 넘겨주세요.
2. 스니펫 이름 규칙: `document("operation-name", ...)`의 첫 번째 인자가 스니펫 폴더명이 됩니다. 예) `document("product-get", ...)` →
   `build/generated-snippets/product-get`.
3. 자동 목차 반영: `generateRestDocsIndex`가 스니펫 폴더를 순회해 `.adoc`을 자동 구성합니다. 테스트만 추가하면 `build/docs/asciidoc/index.html`에 새 섹션이
   생깁니다.
4. 커스텀 템플릿: `src/test/resources/org/springframework/restdocs/templates/asciidoctor` 아래의 `.snippet` 파일로 표 레이아웃을 바꿀 수
   있습니다. 필요하다면 `request-fields.snippet` 등 다른 템플릿도 동일한 구조로 추가하세요.
5. 레이아웃 조정: `src/docs/asciidoc/index.adoc`에서 Asciidoctor 속성을 조정(`:toclevels:` 등)하거나, `auto-index.adoc` 대신
   `operation::operation-name[...]` 매크로를 사용해 수동으로 문서 구조를 설계할 수도 있습니다.

### 생성된 문서 확인(Windows)

start build/docs/asciidoc/index.html

### 참고할 예시 테스트

`ProductControllerRestDocsTest.java`

## 모니터링 대시보드

### 접속 정보

- **Grafana**: http://localhost:3000
    - ID: `admin`
    - PW: `admin`
- **Prometheus**: http://localhost:9091

### 대시보드 종류

#### 1. Uppick 스프링 부트 모니터링 (전체 서비스 모니터링)

기본적인 서비스 헬스 체크 및 성능 모니터링

**주요 메트릭:**

- CPU 사용률
- JVM 메모리 (Heap)
- HTTP 요청 수 (req/s)
- 평균 응답 시간
- JVM 스레드 수
- 데이터베이스 커넥션 (HikariCP)

**사용 방법:**

1. Grafana 접속 (http://localhost:3000)
2. 좌측 메뉴 → Dashboards
3. "Uppick Spring Boot Monitoring" 선택
4. 상단 드롭다운에서 서비스 선택 (All, uppick-main-service, uppick-auction-service)

#### 2. Uppick 동시성 테스트 대시보드 (성능 테스트 전용)

동시성 제어 및 부하 테스트 시 실시간 모니터링

**주요 메트릭:**

- 🔥 **현재 TPS**: 초당 처리 중인 트랜잭션 수
- 🧵 **활성 스레드**: 동시 처리 중인 스레드 수
- 🗄️ **활성 DB 커넥션**: 데이터베이스 커넥션 사용 현황 (병목 확인)
- ❌ **에러율**: 5xx 에러 비율
- 📈 **TPS 추이**: 시간별 요청 처리량
- ⚡ **평균 응답 시간**: 엔드포인트별 응답 시간
- 🧵 **JVM 스레드 상태**: 활성/최대/데몬 스레드
- 🗄️ **DB 커넥션 풀 상태**: 활성/유휴/대기 커넥션
- 💻 **CPU 사용률**: 시스템/프로세스 CPU
- 🧠 **JVM 메모리 사용률**: Heap 메모리 압박
- 📊 **HTTP 상태 코드 분포**: 200/404/5xx 에러 패턴

**사용 방법:**

1. Grafana 접속 (http://localhost:3000)
2. "Uppick 동시성 테스트 대시보드" 선택
3. 상단 드롭다운에서 모니터링할 서비스 선택
4. 부하 테스트 도구(JMeter, Gatling 등)로 요청 발생
5. 실시간으로 동시성 처리 성능 확인

**동시성 테스트 체크리스트:**

- [ ] TPS 증가 시 응답 시간이 급증하는가? → 스레드 풀 병목
- [ ] 활성 DB 커넥션이 최대치(10)에 도달하는가? → DB 커넥션 풀 부족
- [ ] 에러율이 증가하는가? → 동시성 제어 실패
- [ ] CPU 사용률이 100%에 근접하는가? → 리소스 부족
- [ ] 메모리 사용률이 지속적으로 증가하는가? → 메모리 누수 의심

### 추가 대시보드 임포트

Grafana 공식 대시보드를 임포트하여 더 상세한 모니터링이 가능합니다.

#### JVM (Micrometer) 대시보드 임포트 (ID: 4701)

1. Grafana 접속 (http://localhost:3000)
2. 좌측 메뉴 → Dashboards → New → Import
3. "Import via grafana.com" 입력란에 `4701` 입력
4. Load 버튼 클릭
5. Prometheus 데이터소스 선택
6. Import 클릭

**대시보드 4701 주요 메트릭:**

- JVM 메모리 상세 분석 (Eden, Survivor, Old Gen)
- GC (Garbage Collection) 통계
- 클래스 로딩 정보
- 버퍼 풀 사용량
- 로그백 이벤트

### 모니터링 설정

#### 데이터 수집 주기

- Prometheus scrape interval: 15초
- Grafana refresh: 5초

#### 데이터 보관 기간

- Prometheus: 1일 (테스트 환경)

#### 서비스별 포트

- Main Service: 8081
- Auction Service: 8082
- API Gateway: 8080

### Q&A

**Q: 대시보드에서 NoData가 표시됩니다**

- Prometheus에서 메트릭 수집 확인: http://localhost:9091/targets
- 모든 서비스가 UP 상태인지 확인
- 대시보드 상단 시간 범위를 "Last 15 minutes"로 설정
- 변수 드롭다운에서 서비스가 올바르게 선택되었는지 확인 (전체 이름: `uppick-auction-service`)

**Q: 응답 시간 그래프가 안 나옵니다**

- 최소 1~2분 이상 요청을 여러 번 보내야 `rate()` 함수가 작동합니다
- 요청을 10~20회 정도 보낸 후 1분 대기

**Q: TPS가 0으로 표시됩니다**

- 실제 API 요청을 보내야 메트릭이 생성됩니다
- Postman이나 부하 테스트 도구로 엔드포인트에 요청 전송

# k6 Load Testing

**실행:**

```bash
k6 run --out experimental-prometheus-rw \
  -e K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9091/api/v1/write \
  -e K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM=false \
  -e K6_PROMETHEUS_RW_PUSH_INTERVAL=5s \
  k6-tests/find-max-tps-test.js
```

### 시스템 요구사항

k6를 먼저 설치해야 합니다:

https://github.com/grafana/k6/releases/tag/v1.3.0
맨 아래 컴퓨터에 맞춰서 k6 설치(설치 후 인텔리제이 재접속 권장)

#### Grafana 대시보드 (Simplified):

- 👥 Virtual Users
- ⚡ P99 Response Time (Max/Avg/Min)
- 🕐 Request Timing Breakdown
- 🐌 Top 10 Slowest Endpoints

#### k6 테스트 결과 요약 (콘솔):

- 평균/실제 TPS
- 총 요청 수
- 에러율
- 응답시간 (평균/P95/P99)

# k6의 `experimental-prometheus-rw` 출력은 Trend 메트릭(백분위수)만 전송합니다

- ✅ 사용 가능: VU 수, P99 응답시간, Request Timing
- ❌ 사용 불가: TPS, Total Requests, Error Rate (Counter 메트릭 미지원)
- 💡 TPS는 k6 콘솔 출력에서 확인 가능