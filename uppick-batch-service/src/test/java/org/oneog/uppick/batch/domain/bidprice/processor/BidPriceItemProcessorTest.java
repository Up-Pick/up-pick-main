package org.oneog.uppick.batch.domain.bidprice.processor;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.batch.domain.bidprice.dto.BidPriceDto;

@DisplayName("BidPriceItemProcessor 단위 테스트")
class BidPriceItemProcessorTest {

	private final BidPriceItemProcessor processor = new BidPriceItemProcessor();

	@Test
	@DisplayName("정상 데이터 검증 성공")
	void process_validData_success() throws Exception {

		// given
		BidPriceDto input = new BidPriceDto(1L, 10L, 100000L);

		// when
		BidPriceDto result = processor.process(input);

		// then
		assertThat(result).isNotNull();
		assertThat(result).isSameAs(input);
	}

}
