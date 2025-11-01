package org.oneog.uppick.batch.common.config;

import javax.sql.DataSource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;

/**
 * Batch DataSource 설정
 *
 * Batch DB (uppick_batch_db)
 * - Spring Batch 메타데이터: BATCH_JOB_EXECUTION, BATCH_STEP_EXECUTION 등
 * - BatchConfig에서 JobRepository 생성 시 사용
 */
@Slf4j
@Configuration
public class DataSourceConfig {

	@Bean(name = "batchDataSource")
	@ConfigurationProperties(prefix = "batch.datasource")
	public DataSource batchDataSource() {

		log.info("Batch DataSource 초기화 ");
		return DataSourceBuilder.create().build();
	}

}
