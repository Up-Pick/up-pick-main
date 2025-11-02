package org.oneog.uppick.batch.common.config;

import org.oneog.uppick.batch.job.ranking.RankingUpdateTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * 랭킹 업데이트 배치 작업 설정
 * - JobRepository: Batch DB에 메타데이터 저장 (BATCH_JOB_EXECUTION 등)
 * - TransactionManager: Batch DB 트랜잭션 관리 (Step 실행용)
 */
@Slf4j
@Configuration
public class RankingBatchJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final RankingUpdateTasklet rankingUpdateTasklet;

	/**
	 * 생성자 주입
	 * - jobRepository: Batch DB 메타데이터 관리
	 * - transactionManager: Batch DB 트랜잭션 관리 (Step 실행용)
	 */
	public RankingBatchJobConfig(
		JobRepository jobRepository,
		@Qualifier("batchTransactionManager") PlatformTransactionManager transactionManager,
		RankingUpdateTasklet rankingUpdateTasklet) {

		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.rankingUpdateTasklet = rankingUpdateTasklet;
	}

	@Bean
	public Job rankingUpdateJob() {

		return new JobBuilder("rankingUpdateJob", jobRepository)
			.start(rankingUpdateStep())
			.build();
	}

	@Bean
	public Step rankingUpdateStep() {

		return new StepBuilder("rankingUpdateStep", jobRepository)
			.tasklet(rankingUpdateTasklet, transactionManager)
			.build();
	}

}
