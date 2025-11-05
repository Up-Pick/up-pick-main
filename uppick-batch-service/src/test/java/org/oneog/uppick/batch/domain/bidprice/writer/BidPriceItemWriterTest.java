package org.oneog.uppick.batch.domain.bidprice.writer;

import static org.assertj.core.api.AssertionsForClassTypes.*;
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
	@DisplayName("write - 정상 데이터 - ES 업데이트 성공")
	void write_정상데이터_ES업데이트성공() throws Exception {

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

	@Test
	@DisplayName("write - ES 연결 실패 - 예외 발생")
	void write_ES연결실패_예외발생() {

		// given
		BidPriceDto data = new BidPriceDto(1L, 10L, 100000L);
		Chunk<BidPriceDto> chunk = new Chunk<>(List.of(data));
		when(elasticsearchOperations.update(any(UpdateQuery.class), any(IndexCoordinates.class)))
			.thenThrow(new RuntimeException("ES connection failed"));

		// when & then
		assertThatThrownBy(() -> writer.write(chunk))
			.isInstanceOf(RuntimeException.class);
	}

	@Test
	@DisplayName("write - 빈 Chunk - 예외 없이 처리")
	void write_빈Chunk_예외없이처리() throws Exception {

		// given
		Chunk<BidPriceDto> chunk = new Chunk<>();

		// when & then
		assertThatCode(() -> writer.write(chunk)).doesNotThrowAnyException();
		verify(elasticsearchOperations, never()).update(any(), any());
	}

}
