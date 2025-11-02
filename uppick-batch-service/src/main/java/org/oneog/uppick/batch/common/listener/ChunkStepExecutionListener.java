package org.oneog.uppick.batch.common.listener;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Chunk 방식 배치의 공통 StepExecutionListener
 *
 * - beforeStep: Reader 초기화 및 시작 로그
 * - afterStep: 처리 결과 로그 (읽은 데이터, 처리된 데이터, 쓰여진 데이터)
 */
@Slf4j
@RequiredArgsConstructor
public class ChunkStepExecutionListener implements StepExecutionListener {

	private final String batchName;
	private final Runnable readerInitializer;

	@Override
	public void beforeStep(StepExecution stepExecution) {

		log.info("{} 배치 Step 시작", batchName);
		readerInitializer.run(); // Reader 초기화
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {

		log.info("{} 배치 Step 완료 - 읽은 데이터: {}, 처리된 데이터: {}, 쓰여진 데이터: {}",
			batchName,
			stepExecution.getReadCount(),
			stepExecution.getFilterCount(),
			stepExecution.getWriteCount());

		return stepExecution.getExitStatus();
	}

}
