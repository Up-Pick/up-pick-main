import http from 'k6/http';
import {check, sleep} from 'k6';

// ⚙️ 환경 변수 (기본값 포함)
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';  // 테스트 대상 API
const MAX_VUS = Number(__ENV.MAX_VUS) || 500;                // 최대 동시 접속자 수
const RAMP_DURATION = __ENV.RAMP_DURATION || '2m';           // 증가 시간 (2분)
const HOLD_DURATION = __ENV.HOLD_DURATION || '3m';           // 유지 시간 (3분)

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
        http_req_duration: ['p(95)<2000'],     // 95% 요청 2초 이하
    },
};

// 🧠 테스트 로직
export default function () {

    const res = http.get(`http://localhost:8080/auction/api/v1/products/1`, {
        headers: {'Content-Type': 'application/json'},
        timeout: '30s',
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 2000ms': (r) => r.timings.duration < 2000,
    });

    sleep(1); // 평균 대기 시간 (Think Time)
}

// 📊 결과 요약
export function handleSummary(data) {
    const testDuration = data.state.testRunDurationMs / 1000;
    const totalRequests = data.metrics.http_reqs.values.count;
    const avgTPS = totalRequests / testDuration;
    const errorRate = data.metrics.http_req_failed?.values.rate || 0;

    console.log('\n========================================');
    console.log('📊 점진 부하 테스트 결과 요약');
    console.log('========================================');
    console.log(`API 엔드포인트: http://localhost:8080/auction/api/v1/products/1`);
    console.log(`테스트 시간: ${testDuration.toFixed(2)}초`);
    console.log(`최대 동시 접속자(VU): ${MAX_VUS}명`);
    console.log('----------------------------------------');
    console.log(`평균 TPS: ${avgTPS.toFixed(2)}`);
    console.log(`총 요청 수: ${totalRequests}`);
    console.log(`에러율: ${(errorRate * 100).toFixed(2)}%`);
    console.log(`평균 응답시간: ${data.metrics.http_req_duration.values.avg.toFixed(2)}ms`);
    console.log(`P95 응답시간: ${data.metrics.http_req_duration.values['p(95)'].toFixed(2)}ms`);
    console.log(`P99 응답시간: ${data.metrics.http_req_duration.values['p(99)'].toFixed(2)}ms`);
    console.log('========================================\n');

    return {stdout: '\n'};
}
