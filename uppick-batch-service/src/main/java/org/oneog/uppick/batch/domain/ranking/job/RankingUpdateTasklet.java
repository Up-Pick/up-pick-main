package org.oneog.uppick.batch.domain.ranking.job;

import org.oneog.uppick.batch.domain.ranking.client.RankClient;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingUpdateTasklet implements Tasklet {

	private final RankClient rankClient;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		log.debug(" 핫 키워드 랭킹 업데이트 시작");

		try {
			// Main Service Internal API 호출
			rankClient.updateHotKeywords();

			log.debug("핫 키워드 랭킹 업데이트 완료");

			return RepeatStatus.FINISHED;

		} catch (Exception e) {
			log.error("핫 키워드 랭킹 업데이트 실패: {}", e.getMessage(), e);
			throw e;
		}
	}

}
