import http from 'k6/http';
import {check, sleep} from 'k6';
import encoding from 'k6/encoding';

// ⚙️ 시나리오 상수 (제공된 시나리오 기반)
const MAX_VUS = Number(__ENV.MAX_VUS) || 100;              // 최대 동시 접속자 수
const THINK_TIME = Number(__ENV.THINK_TIME) || 5;                // 사용자의 행동 간 평균 시간 (5초)
const READ_RATIO = Number(__ENV.READ_RATIO) || 0.95;             // 읽기 비율 (95%, 일반적인 웹사이트 비율)

const RAMP_DURATION = __ENV.RAMP_DURATION || '100s';         // 부하 증가 시간
const HOLD_DURATION = __ENV.HOLD_DURATION || '300s';          // 최대 부하 유지 시간

// 🔐 인증 토큰
const AUTH_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI2IiwibWVtYmVyTmlja25hbWUiOiLrsJXsiJjtmIQiLCJleHAiOjE3NjIxNzkzNjAsImlhdCI6MTc2MjE3NTc2MH0.4F6IayPwPeaP8h7V5REZLWT97rTumazi5nTC5LysgUI';

// 📍 API 엔드포인트
const BASE_URL = 'http://localhost:8080/auction/api/v1/products';
const READ_URL = `${BASE_URL}/selling/me`;
const WRITE_URL = BASE_URL;

// 🎨 테스트용 더미 이미지 (1x1 픽셀 투명 PNG, base64)
const DUMMY_IMAGE_BASE64 = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==';
const DUMMY_IMAGE_BYTES = encoding.b64decode(DUMMY_IMAGE_BASE64);
const DUMMY_IMAGE = http.file(DUMMY_IMAGE_BYTES, 'test.png', 'image/png');

// 카테고리 ID 리스트
const CATEGORY_IDS = [1, 2, 3, 4, 5];

// 🧩 테스트 옵션
export const options = {
    scenarios: {
        ramp_and_hold: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                {duration: RAMP_DURATION, target: MAX_VUS},
                {duration: HOLD_DURATION, target: MAX_VUS},
            ],
            gracefulRampDown: '30s',
        },
    },

    // 🎯 성능 목표 임계값 (Thresholds)
    thresholds: {
        http_req_failed: ['rate<0.05'],      // 전체 실패율 5% 미만

        // 목표 평균 응답시간(1초)을 P95 기준으로 설정 (더 엄격하고 현실적인 지표)
        'http_req_duration{type:read}': ['p(95)<1000'],   // [목표] 읽기 요청의 95%는 1초(1000ms) 안에 처리
        'http_req_duration{type:write}': ['p(95)<3000'],  // [목표] 쓰기 요청의 95%는 3초(3000ms) 안에 처리
    },
};

// 🧠 테스트 로직
export default function () {
    const isRead = Math.random() < READ_RATIO;

    if (isRead) {
        // ========== 📖 읽기 테스트 (조회) ==========
        const readHeaders = {'Authorization': `Bearer ${AUTH_TOKEN}`};
        const readRes = http.get(READ_URL, {
            headers: readHeaders,
            tags: {type: 'read'},
        });
        check(readRes, {'[READ] status is 200': (r) => r.status === 200});

    } else {
        // ========== ✍️ 쓰기 테스트 (물품 등록) ==========
        const now = new Date();
        const endDate = new Date(now.getTime() + (Math.floor(Math.random() * 13) + 1) * 24 * 60 * 60 * 1000);
        const productData = {
            name: `테스트상품_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
            description: `k6 부하 테스트용 상품입니다. 생성 시각: ${now.toISOString()}`,
            categoryId: CATEGORY_IDS[Math.floor(Math.random() * CATEGORY_IDS.length)],
            startBid: Math.floor(Math.random() * 90000) + 10000,
            endAt: endDate.toISOString().slice(0, -1),
        };
        const formData = {
            product: http.file(JSON.stringify(productData), 'product.json', 'application/json'),
            image: DUMMY_IMAGE,
        };
        const writeHeaders = {'Authorization': `Bearer ${AUTH_TOKEN}`};
        const writeRes = http.post(WRITE_URL, formData, {
            headers: writeHeaders,
            tags: {type: 'write'},
        });
        check(writeRes, {'[WRITE] status is 200': (r) => r.status === 200});
    }

    sleep(THINK_TIME); // 시나리오에 정의된 Think Time (5초) 적용
}


// 📊 결과 요약 (수정 없음)
export function handleSummary(data) {
    const testDuration = data.state.testRunDurationMs / 1000;
    const totalRequests = data.metrics.http_reqs.values.count;
    const avgTPS = totalRequests / testDuration;
    const errorRate = data.metrics.http_req_failed?.values.rate || 0;

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