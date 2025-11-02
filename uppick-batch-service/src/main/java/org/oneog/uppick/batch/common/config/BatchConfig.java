package org.oneog.uppick.batch.common.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.batch.BatchDataSource;
import org.springframework.boot.autoconfigure.batch.BatchDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(BatchProperties.class)
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
	 * Batch 메타데이터용 TransactionManager 지정
	 * DefaultBatchConfiguration이 JobRepository 생성 시 사용
	 */
	@Override
	protected PlatformTransactionManager getTransactionManager() {

		log.info("Batch TransactionManager 설정 (DefaultBatchConfiguration용)");
		return new JdbcTransactionManager(batchDataSource);
	}

	/**
	 * Batch TransactionManager Bean
	 * Step에서 @Qualifier로 주입받기 위해 Bean으로 노출
	 */
	@Bean(name = "batchTransactionManager")
	public PlatformTransactionManager batchTransactionManager() {

		log.info("Batch TransactionManager Bean 생성");
		return getTransactionManager(); // 동일한 TransactionManager 반환
	}

	/**
	 * Batch 메타데이터 테이블 자동 생성
	 * spring.batch.jdbc.initialize-schema 설정을 읽어서 스키마 초기화
	 */
	@Bean
	public BatchDataSourceScriptDatabaseInitializer batchDataSourceScriptDatabaseInitializer(
		@Qualifier("batchDataSource") DataSource dataSource,
		BatchProperties properties
	) {

		log.info("Batch 스키마 초기화 설정: {}", properties.getJdbc().getInitializeSchema());
		return new BatchDataSourceScriptDatabaseInitializer(dataSource, properties.getJdbc());
	}

}
