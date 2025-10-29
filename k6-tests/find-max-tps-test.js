import http from 'k6/http';
import { check } from 'k6';
import { Rate, Counter } from 'k6/metrics';

const errorRate = new Rate('errors');
const requestCounter = new Counter('total_requests');

// 단계적으로 TPS 올리면서 한계 찾기
export const options = {
  scenarios: {
    find_max_tps: {
      executor: 'ramping-arrival-rate',
      startRate: 50,
      timeUnit: '1s',
      preAllocatedVUs: 300,
      maxVUs: 1500,
      stages: [
        { duration: '1m', target: 50 },    // 50 TPS
        { duration: '1m', target: 100 },   // 100 TPS
        { duration: '1m', target: 150 },   // 150 TPS
        { duration: '1m', target: 200 },   // 200 TPS
        { duration: '1m', target: 250 },   // 250 TPS
        { duration: '1m', target: 300 },   // 300 TPS (도전)
      ],
    },
  },
  
  thresholds: {
    'http_req_duration': ['p(95)<2000'],
    'http_req_failed': ['rate<0.2'],  // 20%까지 허용
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  const productId = Math.floor(Math.random() * 100) + 1;
  
  const response = http.get(`${BASE_URL}/auction/api/v1/products/${productId}`, {
    headers: { 'Content-Type': 'application/json' },
    timeout: '30s',
  });
  
  const success = check(response, {
    'status is 200': (r) => r.status === 200,
    'has response': (r) => r.body && r.body.length > 0,
  });
  
  if (!success) errorRate.add(1);
  requestCounter.add(1);
}

export function handleSummary(data) {
  console.log('\n' + '='.repeat(70));
  console.log('🔍 최대 TPS 한계 찾기 - 단계적 부하 증가 테스트');
  console.log('='.repeat(70) + '\n');
  
  const totalRequests = data.metrics.http_reqs?.values.count || 0;
  const actualTPS = data.metrics.http_reqs?.values.rate || 0;
  const avgDuration = data.metrics.http_req_duration?.values.avg || 0;
  const p95Duration = data.metrics.http_req_duration?.values['p(95)'] || 0;
  const p99Duration = data.metrics.http_req_duration?.values['p(99)'] || 0;
  const failedRequests = data.metrics.http_req_failed?.values.passes || 0;
  const errorRateValue = (data.metrics.http_req_failed?.values.rate || 0) * 100;
  
  console.log('📊 최종 결과');
  console.log('─'.repeat(70));
  console.log(`  실제 평균 TPS:    ${actualTPS.toFixed(2)} req/s`);
  console.log(`  총 요청 수:       ${totalRequests.toLocaleString()}개`);
  console.log(`  실패 요청:        ${failedRequests}개`);
  console.log(`  에러율:           ${errorRateValue.toFixed(2)}%`);
  console.log('');
  
  console.log('⏱️  응답 시간');
  console.log('─'.repeat(70));
  console.log(`  평균:             ${avgDuration.toFixed(2)}ms`);
  console.log(`  p(95):            ${p95Duration.toFixed(2)}ms`);
  console.log(`  p(99):            ${p99Duration.toFixed(2)}ms`);
  console.log('');
  
  console.log('📈 수용 인원 계산 (Think Time 10초)');
  console.log('─'.repeat(70));
  const concurrentUsers = Math.floor(actualTPS * 10);
  const dau = Math.floor(concurrentUsers / 0.1);
  const mau = Math.floor(dau / 0.15);
  console.log(`  동시 접속자:      ${concurrentUsers.toLocaleString()}명`);
  console.log(`  DAU:              ${dau.toLocaleString()}명`);
  console.log(`  MAU:              ${mau.toLocaleString()}명`);
  console.log('');
  
  console.log('💡 TPS 단계별 결과 분석');
  console.log('─'.repeat(70));
  console.log(`  50 TPS:           ${actualTPS >= 50 ? '✅ 통과' : '❌ 실패'}`);
  console.log(`  100 TPS:          ${actualTPS >= 100 ? '✅ 통과' : '❌ 실패'}`);
  console.log(`  150 TPS:          ${actualTPS >= 150 ? '✅ 통과' : '❌ 실패'}`);
  console.log(`  200 TPS:          ${actualTPS >= 200 ? '✅ 통과' : '❌ 실패'}`);
  console.log(`  250 TPS:          ${actualTPS >= 250 ? '✅ 통과' : '❌ 실패'}`);
  console.log(`  300 TPS (목표):   ${actualTPS >= 300 ? '🎉 달성!' : '❌ 미달'}`);
  console.log('');
  
  console.log('🎯 결론');
  console.log('─'.repeat(70));
  if (errorRateValue < 1) {
    console.log(`  서버 안정 TPS:    약 ${Math.floor(actualTPS)} req/s`);
    console.log(`  여유 있음, 더 높은 부하 가능`);
  } else if (errorRateValue < 5) {
    console.log(`  서버 한계 TPS:    약 ${Math.floor(actualTPS * 0.9)} req/s`);
    console.log(`  현재 수준이 최대 안정 지점`);
  } else if (errorRateValue < 10) {
    console.log(`  서버 한계 도달:   약 ${Math.floor(actualTPS * 0.8)} req/s`);
    console.log(`  에러율 증가, 최적화 필요`);
  } else {
    console.log(`  서버 과부하:      약 ${Math.floor(actualTPS * 0.7)} req/s까지 안정`);
    console.log(`  현재 부하는 과도함`);
  }
  
  console.log('');
  console.log('='.repeat(70) + '\n');
  
  return {
    'stdout': '',
    'summary.json': JSON.stringify(data, null, 2),
  };
}
