package org.oneog.uppick.batch.domain.bidprice.writer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.batch.domain.bidprice.dto.BidPriceDto;
import org.springframework.batch.item.Chunk;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidPriceItemWriter 단위 테스트")
class BidPriceItemWriterTest {

	@Mock
	private ElasticsearchOperations elasticsearchOperations;

	@InjectMocks
	private BidPriceItemWriter writer;

	@Test
	@DisplayName("ES 업데이트 성공")
	void write_success() throws Exception {

		// given
		BidPriceDto data = new BidPriceDto(1L, 10L, 100000L);
		Chunk<BidPriceDto> chunk = new Chunk<>(List.of(data));
		when(elasticsearchOperations.update(any(UpdateQuery.class), any(IndexCoordinates.class)))
			.thenReturn(null);

		// when
		writer.write(chunk);

		// then
		verify(elasticsearchOperations, times(1)).update(any(UpdateQuery.class), any(IndexCoordinates.class));
	}

}
