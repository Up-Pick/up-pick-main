package org.oneog.uppick.batch.common.config;

import org.oneog.uppick.batch.job.ranking.RankingUpdateTasklet;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RankingBatchJobConfig {

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final RankingUpdateTasklet rankingUpdateTasklet;

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
