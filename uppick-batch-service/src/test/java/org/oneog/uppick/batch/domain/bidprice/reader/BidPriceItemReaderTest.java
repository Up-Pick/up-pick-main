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
	@DisplayName("정상 데이터 읽기 성공")
	void read_success() throws Exception {

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
	@DisplayName("Redis에 데이터가 없을 때 null 반환")
	void read_emptyRedis_returnsNull() throws Exception {

		// given
		when(stringRedisTemplate.keys("auction:*:current-bid-price")).thenReturn(Set.of());

		// when
		reader.init();
		BidPriceDto result = reader.read();

		// then
		assertThat(result).isNull();
	}

}
