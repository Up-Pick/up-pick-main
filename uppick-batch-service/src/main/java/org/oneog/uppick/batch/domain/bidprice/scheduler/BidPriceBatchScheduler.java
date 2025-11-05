package org.oneog.uppick.batch.domain.bidprice.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 입찰가 배치 스케줄러
 *
 * 매 1분마다 Redis의 입찰가를 Elasticsearch에 동기화
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BidPriceBatchScheduler {

	private final JobLauncher jobLauncher;
	private final Job bidPriceUpdateJob;

	@Scheduled(cron = "0 * * * * *") // 매분 실행 (0초마다)
	public void runBidPriceUpdateBatch() {

		log.debug("입찰가 배치 Job 실행 시작");

		try {
			// JobParameters에 현재 시간을 넣어 매번 다른 Job Instance 생성
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(bidPriceUpdateJob, jobParameters);

			log.debug("입찰가 배치 Job 실행 완료");
		} catch (Exception e) {
			log.error("입찰가 배치 Job 실행 실패: {}", e.getMessage(), e);
		}
	}

}
