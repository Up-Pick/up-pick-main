package org.oneog.uppick.batch.domain.productviewcount.writer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.batch.domain.productviewcount.dto.ViewCountDto;
import org.springframework.batch.item.Chunk;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("ViewCountItemWriter 단위 테스트")
class ViewCountItemWriterTest {

	@Mock
	private DataSource auctionDataSource;

	@Mock
	private JdbcTemplate jdbcTemplate;

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	private ViewCountItemWriter writer;

	@BeforeEach
	void setUp() {

		writer = new ViewCountItemWriter(auctionDataSource, stringRedisTemplate);
		ReflectionTestUtils.setField(writer, "jdbcTemplate", jdbcTemplate);
	}

	@Test
	@DisplayName("DB 업데이트 및 Redis 삭제 성공")
	void write_success() throws Exception {

		// given
		ViewCountDto data = new ViewCountDto(10L, 5L);
		Chunk<ViewCountDto> chunk = new Chunk<>(List.of(data));
		when(jdbcTemplate.update(anyString(), anyLong(), anyLong())).thenReturn(1);
		when(stringRedisTemplate.delete("product:view:10")).thenReturn(true);

		// when
		writer.write(chunk);

		// then
		verify(jdbcTemplate, times(1)).update(anyString(), anyLong(), anyLong());
		verify(stringRedisTemplate, times(1)).delete("product:view:10");
	}

}
