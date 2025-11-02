package org.oneog.uppick.batch.common.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Batch 설정
 *
 * DefaultBatchConfiguration을 확장하여 Batch DB 사용
 * - BATCH_JOB_EXECUTION, BATCH_STEP_EXECUTION 등 메타데이터 저장
 */
@Slf4j
@Configuration
public class BatchConfig extends DefaultBatchConfiguration {

	private final DataSource batchDataSource;

	public BatchConfig(@Qualifier("batchDataSource") DataSource batchDataSource) {

		this.batchDataSource = batchDataSource;
		log.info("BatchConfig 초기화 ");
	}

	/**
	 * Batch 메타데이터용 DataSource 지정
	 */
	@Override
	protected DataSource getDataSource() {

		log.info("Batch DataSource 설정");
		return batchDataSource;
	}

	/**
	 * Batch TransactionManager Bean
	 * Step에서 트랜잭션 관리를 위해 Bean으로 노출
	 */
	@Bean(name = "batchTransactionManager")
	public PlatformTransactionManager batchTransactionManager() {

		log.info("Batch TransactionManager Bean 생성");
		return new JdbcTransactionManager(batchDataSource);
	}

}
