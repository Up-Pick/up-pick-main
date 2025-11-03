import http from 'k6/http';
import {check, sleep} from 'k6';
import encoding from 'k6/encoding';

// ⚙️ 환경 변수 (기본값 포함)
const MAX_VUS = Number(__ENV.MAX_VUS) || 500;                // 최대 동시 접속자 수
const RAMP_DURATION = __ENV.RAMP_DURATION || '2m';           // 증가 시간 (2분)
const HOLD_DURATION = __ENV.HOLD_DURATION || '3m';           // 유지 시간 (3분)
const READ_RATIO = Number(__ENV.READ_RATIO) || 0.7;          // 읽기 비율 (70%)

// 🔐 인증 토큰
const AUTH_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2IiwibWVtYmVyTmlja25hbWUiOiLrsJXsiJjtmIQxIiwiZXhwIjoxNzYyMDk5NTUwLCJpYXQiOjE3NjIwOTU5NTB9.IYdM75SY3DuNe4bv3lwQT8WVhG0-OliBTRepLMAzwBY';

// 📍 API 엔드포인트
const BASE_URL = 'http://localhost:8080/auction/api/v1/products';
const READ_URL = `${BASE_URL}/selling/me`;  // 내 판매 상품 목록 조회
const WRITE_URL = BASE_URL;        // 등록

// 🎨 테스트용 더미 이미지 (1x1 픽셀 투명 PNG, base64)
const DUMMY_IMAGE_BASE64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
const DUMMY_IMAGE_BYTES = encoding.b64decode(DUMMY_IMAGE_BASE64);
const DUMMY_IMAGE = http.file(DUMMY_IMAGE_BYTES, 'test.png', 'image/png');

// 카테고리 ID 리스트 (실제 DB에 있는 카테고리 ID로 수정 필요!)
const CATEGORY_IDS = [1, 2, 3, 4, 5];

// 🧩 테스트 옵션
export const options = {
    scenarios: {
        ramp_and_hold: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                {duration: RAMP_DURATION, target: MAX_VUS},  // ⬆️ 0 → 500으로 점진 증가
                {duration: HOLD_DURATION, target: MAX_VUS},  // ⏸️ 500명 유지
                // { duration: '1m', target: 0 },              // ⬇️ (선택) ramp-down
            ],
            gracefulRampDown: '30s',                         // 종료 시 부드럽게 감소
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.05'],        // 실패율 5% 미만
        http_req_duration: ['p(95)<3000'],     // 95% 요청 3초 이하 (이미지 업로드 고려)
        'http_req_duration{type:read}': ['p(95)<2000'],   // 읽기 요청 2초 이하
        'http_req_duration{type:write}': ['p(95)<5000'],  // 쓰기 요청 5초 이하
    },
};

// 🧠 테스트 로직
export default function () {
    // 70% 읽기, 30% 쓰기 (READ_RATIO로 조절 가능)
    const isRead = Math.random() < READ_RATIO;

    if (isRead) {
        // ========== 📖 읽기 테스트 (조회) ==========
        const readHeaders = {
            'Authorization': `Bearer ${AUTH_TOKEN}`,
        };

        const readRes = http.get(READ_URL, {
            headers: readHeaders,
            timeout: '30s',
            tags: {type: 'read'},
        });

        check(readRes, {
            '[READ] status is 200': (r) => r.status === 200,
            '[READ] response time < 2000ms': (r) => r.timings.duration < 2000,
        });

    } else {
        // ========== ✍️ 쓰기 테스트 (물품 등록) ==========

        // 1. ProductRegisterRequest JSON 생성
        const now = new Date();
        const endDate = new Date(now.getTime() + (Math.floor(Math.random() * 13) + 1) * 24 * 60 * 60 * 1000); // 1~13일 후

        const productData = {
            name: `테스트상품_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
            description: `k6 부하 테스트용 상품입니다. 생성 시각: ${now.toISOString()}`,
            categoryId: CATEGORY_IDS[Math.floor(Math.random() * CATEGORY_IDS.length)],
            startBid: Math.floor(Math.random() * 90000) + 10000, // 10,000 ~ 100,000
            endAt: endDate.toISOString().slice(0, -1), // ISO 8601 형식 (밀리초 제거)
        };

        // 2. Multipart Form Data 생성
        const formData = {
            product: http.file(JSON.stringify(productData), 'product.json', 'application/json'),
            image: DUMMY_IMAGE,
        };

        const writeHeaders = {
            'Authorization': `Bearer ${AUTH_TOKEN}`,
            // Content-Type은 자동으로 설정됨 (multipart/form-data; boundary=...)
        };

        const writeRes = http.post(WRITE_URL, formData, {
            headers: writeHeaders,
            timeout: '30s',
            tags: {type: 'write'},
        });

        const writeSuccess = check(writeRes, {
            '[WRITE] status is 200': (r) => r.status === 200,
            '[WRITE] response time < 5000ms': (r) => r.timings.duration < 5000,
        });

        // 에러 로깅 (디버깅용)
        if (!writeSuccess) {
            console.log(`❌ 물품 등록 실패: ${writeRes.status} - ${writeRes.body}`);
        }
    }

    sleep(1); // 평균 대기 시간 (Think Time)
}

// 📊 결과 요약
export function handleSummary(data) {
    const testDuration = data.state.testRunDurationMs / 1000;
    const totalRequests = data.metrics.http_reqs.values.count;
    const avgTPS = totalRequests / testDuration;
    const errorRate = data.metrics.http_req_failed?.values.rate || 0;

    // 읽기/쓰기 분리 통계
    const readMetrics = data.metrics['http_req_duration{type:read}'];
    const writeMetrics = data.metrics['http_req_duration{type:write}'];

    console.log('\n========================================');
    console.log('📊 혼합 부하 테스트 결과 요약');
    console.log(`(읽기 ${(READ_RATIO * 100).toFixed(0)}% + 쓰기 ${((1 - READ_RATIO) * 100).toFixed(0)}%)`);
    console.log('========================================');
    console.log(`API 엔드포인트: ${BASE_URL}`);
    console.log(`테스트 시간: ${testDuration.toFixed(2)}초`);
    console.log(`최대 동시 접속자(VU): ${MAX_VUS}명`);
    console.log('----------------------------------------');
    console.log(`평균 TPS: ${avgTPS.toFixed(2)}`);
    console.log(`총 요청 수: ${totalRequests}`);
    console.log(`전체 에러율: ${(errorRate * 100).toFixed(2)}%`);
    console.log('----------------------------------------');
    console.log(`[전체] 평균 응답시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
    console.log(`[전체] P95 응답시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
    console.log(`[전체] P99 응답시간: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`);

    if (readMetrics) {
        console.log('----------------------------------------');
        console.log(`[읽기] 평균 응답시간: ${readMetrics.values.avg.toFixed(2)}ms`);
        console.log(`[읽기] P95 응답시간: ${readMetrics.values['p(95)'].toFixed(2)}ms`);
        console.log(`[읽기] P99 응답시간: ${readMetrics.values['p(99)'].toFixed(2)}ms`);
    }

    if (writeMetrics) {
        console.log('----------------------------------------');
        console.log(`[쓰기] 평균 응답시간: ${writeMetrics.values.avg.toFixed(2)}ms`);
        console.log(`[쓰기] P95 응답시간: ${writeMetrics.values['p(95)'].toFixed(2)}ms`);
        console.log(`[쓰기] P99 응답시간: ${writeMetrics.values['p(99)'].toFixed(2)}ms`);
    }

    console.log('========================================\n');

    return {stdout: '\n'};
}