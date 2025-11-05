package org.oneog.uppick.batch.domain.bidprice.reader;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.batch.domain.bidprice.dto.BidPriceDto;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidPriceItemReader 단위 테스트")
class BidPriceItemReaderTest {

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private ValueOperations<String, String> valueOperations;

	@Mock
	private DataSource auctionDataSource;

	@Mock
	private JdbcTemplate jdbcTemplate;

	private BidPriceItemReader reader;

	@BeforeEach
	void setUp() {

		reader = new BidPriceItemReader(stringRedisTemplate, auctionDataSource);
		ReflectionTestUtils.setField(reader, "auctionJdbcTemplate", jdbcTemplate);
	}

	@Test
	@DisplayName("read - 정상 데이터 - BidPriceDto 반환")
	void read_정상데이터_BidPriceDto반환() throws Exception {

		// given
		String key = "auction:1:current-bid-price";
		when(stringRedisTemplate.keys("auction:*:current-bid-price")).thenReturn(Set.of(key));
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(key)).thenReturn("100000");
		when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), eq(1L))).thenReturn(10L);

		// when
		reader.init();
		BidPriceDto result = reader.read();

		// then
		assertThat(result).isNotNull();
		assertThat(result.getAuctionId()).isEqualTo(1L);
		assertThat(result.getProductId()).isEqualTo(10L);
		assertThat(result.getBidPrice()).isEqualTo(100000L);
	}

	@Test
	@DisplayName("read - Redis 데이터 없음 - null 반환")
	void read_Redis데이터없음_null반환() throws Exception {

		// given
		when(stringRedisTemplate.keys("auction:*:current-bid-price")).thenReturn(Set.of());

		// when
		reader.init();
		BidPriceDto result = reader.read();

		// then
		assertThat(result).isNull();
	}

	@Test
	@DisplayName("read - 잘못된 형식 데이터 - 스킵하고 정상 데이터 반환")
	void read_잘못된형식데이터_스킵하고정상데이터반환() throws Exception {

		// given
		String key1 = "auction:1:current-bid-price";
		String key2 = "auction:2:current-bid-price";
		when(stringRedisTemplate.keys("auction:*:current-bid-price")).thenReturn(Set.of(key1, key2));
		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
		when(valueOperations.get(anyString())).thenAnswer(invocation -> {
			String key = invocation.getArgument(0);
			if (key.equals(key1))
				return "invalid";
			if (key.equals(key2))
				return "100000";
			return null;
		});
		when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), anyLong())).thenAnswer(invocation -> {
			Long auctionId = invocation.getArgument(2);
			return auctionId * 10;
		});

		// when
		reader.init();
		BidPriceDto result = reader.read();

		// then
		assertThat(result).isNotNull();
		assertThat(result.getAuctionId()).isEqualTo(2L);
	}

}
