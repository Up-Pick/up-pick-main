package org.oneog.uppick.batch.domain.bidprice.reader;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

}
