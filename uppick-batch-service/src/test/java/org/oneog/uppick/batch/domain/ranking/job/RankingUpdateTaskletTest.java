package org.oneog.uppick.batch.domain.ranking.job;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.batch.domain.ranking.client.RankClient;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
@DisplayName("RankingUpdateTasklet 단위 테스트")
class RankingUpdateTaskletTest {

	@Mock
	private RankClient rankClient;

	@Mock
	private StepContribution contribution;

	@Mock
	private ChunkContext chunkContext;

	@InjectMocks
	private RankingUpdateTasklet tasklet;

	@Test
	@DisplayName("execute - 정상 API 호출 - FINISHED 반환")
	void execute_정상API호출_FINISHED반환() throws Exception {

		// given
		doNothing().when(rankClient).updateHotKeywords();

		// when
		RepeatStatus result = tasklet.execute(contribution, chunkContext);

		// then
		assertThat(result).isEqualTo(RepeatStatus.FINISHED);
		verify(rankClient, times(1)).updateHotKeywords();
	}

	@Test
	@DisplayName("execute - API 호출 실패 - 예외 발생")
	void execute_API호출실패_예외발생() {

		// given
		doThrow(new RuntimeException("API call failed")).when(rankClient).updateHotKeywords();

		// when & then
		assertThatThrownBy(() -> tasklet.execute(contribution, chunkContext))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("API call failed");
	}

	@Test
	@DisplayName("execute - 여러 번 실행 - 모두 성공")
	void execute_여러번실행_모두성공() throws Exception {
		
		// given
		doNothing().when(rankClient).updateHotKeywords();

		// when
		RepeatStatus result1 = tasklet.execute(contribution, chunkContext);
		RepeatStatus result2 = tasklet.execute(contribution, chunkContext);

		// then
		assertThat(result1).isEqualTo(RepeatStatus.FINISHED);
		assertThat(result2).isEqualTo(RepeatStatus.FINISHED);
		verify(rankClient, times(2)).updateHotKeywords();
	}

}
