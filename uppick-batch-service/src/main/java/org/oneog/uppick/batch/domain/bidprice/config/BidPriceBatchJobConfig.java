package org.oneog.uppick.batch.domain.bidprice.config;

import org.oneog.uppick.batch.common.listener.ChunkStepExecutionListener;
import org.oneog.uppick.batch.domain.bidprice.dto.BidPriceDto;
import org.oneog.uppick.batch.domain.bidprice.processor.BidPriceItemProcessor;
import org.oneog.uppick.batch.domain.bidprice.reader.BidPriceItemReader;
import org.oneog.uppick.batch.domain.bidprice.writer.BidPriceItemWriter;
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
 * 입찰가 배치 작업 설정 (Chunk 방식)
 *
 * - Reader: Redis에서 auction:*:current-bid-price 키 조회
 * - Processor: 데이터 검증
 * - Writer: Elasticsearch ProductDocument 업데이트
 * - Chunk Size: 100 (100개씩 묶어서 트랜잭션 처리)
 */
@Slf4j
@Configuration
public class BidPriceBatchJobConfig {

	private static final int CHUNK_SIZE = 100;

	private final JobRepository jobRepository;
	private final PlatformTransactionManager transactionManager;
	private final BidPriceItemReader bidPriceItemReader;
	private final BidPriceItemProcessor bidPriceItemProcessor;
	private final BidPriceItemWriter bidPriceItemWriter;

	public BidPriceBatchJobConfig(
		JobRepository jobRepository,
		@Qualifier("batchTransactionManager") PlatformTransactionManager transactionManager,
		BidPriceItemReader bidPriceItemReader,
		BidPriceItemProcessor bidPriceItemProcessor,
		BidPriceItemWriter bidPriceItemWriter
	) {

		this.jobRepository = jobRepository;
		this.transactionManager = transactionManager;
		this.bidPriceItemReader = bidPriceItemReader;
		this.bidPriceItemProcessor = bidPriceItemProcessor;
		this.bidPriceItemWriter = bidPriceItemWriter;
	}

	@Bean
	public Job bidPriceUpdateJob() {

		return new JobBuilder("bidPriceUpdateJob", jobRepository)
			.start(bidPriceUpdateStep())
			.build();
	}

	@Bean
	public Step bidPriceUpdateStep() {

		return new StepBuilder("bidPriceUpdateStep", jobRepository)
			.<BidPriceDto, BidPriceDto>chunk(CHUNK_SIZE, transactionManager)
			.reader(bidPriceItemReader)
			.processor(bidPriceItemProcessor)
			.writer(bidPriceItemWriter)
			.listener(new ChunkStepExecutionListener("입찰가", bidPriceItemReader::init))
			.build();
	}

}
