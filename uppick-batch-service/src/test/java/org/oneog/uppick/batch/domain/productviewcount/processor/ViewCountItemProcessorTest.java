package org.oneog.uppick.batch.domain.productviewcount.processor;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.batch.domain.productviewcount.dto.ViewCountDto;

@DisplayName("ViewCountItemProcessor 단위 테스트")
class ViewCountItemProcessorTest {

	private final ViewCountItemProcessor processor = new ViewCountItemProcessor();

	@Test
	@DisplayName("정상 데이터 검증 성공")
	void process_validData_success() throws Exception {

		// given
		ViewCountDto input = new ViewCountDto(10L, 5L);

		// when
		ViewCountDto result = processor.process(input);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isSameAs(input);
	}

}
