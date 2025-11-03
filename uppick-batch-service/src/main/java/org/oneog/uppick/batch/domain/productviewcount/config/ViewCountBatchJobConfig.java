package org.oneog.uppick.batch.domain.productviewcount.config;

import org.oneog.uppick.batch.common.listener.ChunkStepExecutionListener;
import org.oneog.uppick.batch.domain.productviewcount.dto.ViewCountDto;
import org.oneog.uppick.batch.domain.productviewcount.processor.ViewCountItemProcessor;
import org.oneog.uppick.batch.domain.productviewcount.reader.ViewCountItemReader;
import org.oneog.uppick.batch.domain.productviewcount.writer.ViewCountItemWriter;
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
 * 조회수 배치 작업 설정 (Chunk 방식)
 *
 * - Reader: Redis에서 product:view:* 키 조회
 * - Processor: 데이터 검증
 * - Writer: DB 업데이트 + Redis 키 삭제
 * - Chunk Size: 100 (100개씩 묶어서 트랜잭션 처리)
 */
@Slf4j
@Configuration
public class ViewCountBatchJobConfig {

	private static final int CHUNK_SIZE = 100;

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final ViewCountItemReader viewCountItemReader;
	private final ViewCountItemProcessor viewCountItemProcessor;
	private final ViewCountItemWriter viewCountItemWriter;

	public ViewCountBatchJobConfig(
		JobRepository jobRepository,
		@Qualifier("batchTransactionManager") PlatformTransactionManager transactionManager,
		ViewCountItemReader viewCountItemReader,
		ViewCountItemProcessor viewCountItemProcessor,
		ViewCountItemWriter viewCountItemWriter
	) {

		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.viewCountItemReader = viewCountItemReader;
		this.viewCountItemProcessor = viewCountItemProcessor;
		this.viewCountItemWriter = viewCountItemWriter;
	}

	@Bean
	public Job viewCountUpdateJob() {

		return new JobBuilder("viewCountUpdateJob", jobRepository)
			.start(viewCountUpdateStep())
			.build();
	}

	@Bean
	public Step viewCountUpdateStep() {

		return new StepBuilder("viewCountUpdateStep", jobRepository)
			.<ViewCountDto, ViewCountDto>chunk(CHUNK_SIZE, transactionManager)
			.reader(viewCountItemReader)
			.processor(viewCountItemProcessor)
			.writer(viewCountItemWriter)
			.listener(new ChunkStepExecutionListener("조회수", viewCountItemReader::init))
			.build();
	}

}
