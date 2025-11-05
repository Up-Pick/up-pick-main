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

	@Test
	@DisplayName("null 필드가 있으면 null 반환")
	void process_nullFields_returnsNull() throws Exception {

		// given & when & then
		assertThat(processor.process(new BidPriceDto(null, 10L, 100000L))).isNull();
		assertThat(processor.process(new BidPriceDto(1L, null, 100000L))).isNull();
		assertThat(processor.process(new BidPriceDto(1L, 10L, null))).isNull();
	}

	@Test
	@DisplayName("0 이하 입찰가는 null 반환")
	void process_invalidBidPrice_returnsNull() throws Exception {

		// given & when & then
		assertThat(processor.process(new BidPriceDto(1L, 10L, 0L))).isNull();
		assertThat(processor.process(new BidPriceDto(1L, 10L, -1000L))).isNull();
	}

}
