package org.oneog.uppick.batch.domain.productviewcount.scheduler;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountBatchScheduler {

	private final JobLauncher jobLauncher;
	private final Job viewCountUpdateJob;

	@Scheduled(cron = "0 * * * * *") // 매분 실행 (0초마다)
	public void runViewCountUpdateBatch() {

		log.info("조회수 배치 Job 실행 시작");

		try {
			// JobParameters에 현재 시간을 넣어 매번 다른 Job Instance 생성
			JobParameters jobParameters = new JobParametersBuilder()
				.addLong("timestamp", System.currentTimeMillis())
				.toJobParameters();

			jobLauncher.run(viewCountUpdateJob, jobParameters);

			log.info("조회수 배치 Job 실행 완료");
		} catch (Exception e) {
			log.error("조회수 배치 Job 실행 실패: {}", e.getMessage(), e);
		}
	}

}
