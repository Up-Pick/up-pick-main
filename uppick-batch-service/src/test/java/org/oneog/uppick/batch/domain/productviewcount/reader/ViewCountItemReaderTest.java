package org.oneog.uppick.batch.domain.productviewcount.reader;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.batch.domain.productviewcount.dto.ViewCountDto;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViewCountItemReader 단위 테스트")
class ViewCountItemReaderTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@InjectMocks
	private ViewCountItemReader reader;

	@Test
	@DisplayName("read - 정상 데이터 - ViewCountDto 반환")
	void read_정상데이터_ViewCountDto반환() throws Exception {

		// given
		String key = "product:view:10";
		when(stringRedisTemplate.keys("product:view:*")).thenReturn(Set.of(key));
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn("5");

		// when
		reader.init();
		ViewCountDto result = reader.read();

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProductId()).isEqualTo(10L);
		assertThat(result.getViewCount()).isEqualTo(5L);
	}

	@Test
	@DisplayName("read - Redis 데이터 없음 - null 반환")
	void read_Redis데이터없음_null반환() throws Exception {

		// given
		when(stringRedisTemplate.keys("product:view:*")).thenReturn(Set.of());

		// when
		reader.init();
		ViewCountDto result = reader.read();

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("read - 잘못된 형식 데이터 - 스킵하고 정상 데이터 반환")
	void read_잘못된형식데이터_스킵하고정상데이터반환() throws Exception {

		// given
		String key1 = "product:view:10";
		String key2 = "product:view:20";
		when(stringRedisTemplate.keys("product:view:*")).thenReturn(Set.of(key1, key2));
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			if (key.equals(key1))
				return "invalid";
			if (key.equals(key2))
				return "5";
			return null;
		});

		// when
		reader.init();
		ViewCountDto result = reader.read();

		// then
		assertThat(result).isNotNull();
		assertThat(result.getProductId()).isEqualTo(20L);
	}

}
