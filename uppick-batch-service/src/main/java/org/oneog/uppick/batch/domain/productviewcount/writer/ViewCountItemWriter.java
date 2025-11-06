package org.oneog.uppick.batch.domain.productviewcount.writer;

import javax.sql.DataSource;

import org.oneog.uppick.batch.domain.productviewcount.dto.ViewCountDto;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 조회수 배치 ItemWriter
 *
 * 1. Auction DB의 product 테이블 view_count 업데이트
 * 2. 처리 완료된 Redis 키 삭제
 */
@Slf4j
@Component
public class ViewCountItemWriter implements ItemWriter<ViewCountDto> {

	private static final String REDIS_KEY_PREFIX = "product:view:";
	private static final String UPDATE_SQL = "UPDATE product SET view_count = view_count + ? WHERE id = ?";

	private final JdbcTemplate jdbcTemplate;
	private final StringRedisTemplate stringRedisTemplate;

	public ViewCountItemWriter(
		@Qualifier("auctionDataSource") DataSource auctionDataSource,
		StringRedisTemplate stringRedisTemplate
	) {

		this.jdbcTemplate = new JdbcTemplate(auctionDataSource);
		this.stringRedisTemplate = stringRedisTemplate;
	}

	@Override
	public void write(Chunk<? extends ViewCountDto> chunk) throws Exception {

		log.debug("조회수 배치 Write 시작 - 처리할 데이터: {}개", chunk.size());

		for (ViewCountDto data : chunk) {
			try {
				// 1. DB 업데이트 (조회수 증가)
				int updatedRows = jdbcTemplate.update(UPDATE_SQL, data.getViewCount(), data.getProductId());

				if (updatedRows > 0) {
					log.debug("productId {}의 조회수 {}만큼 증가 완료", data.getProductId(), data.getViewCount());

					// 2. Redis 키 삭제
					String redisKey = REDIS_KEY_PREFIX + data.getProductId();
					Boolean deleted = stringRedisTemplate.delete(redisKey);

					if (Boolean.TRUE.equals(deleted)) {
						log.debug("Redis 키 삭제 완료: {}", redisKey);
					} else {
						log.warn("Redis 키 삭제 실패: {}", redisKey);
					}
				} else {
					log.warn("productId {}에 해당하는 상품이 DB에 존재하지 않습니다.", data.getProductId());
				}

			} catch (Exception e) {
				log.error("조회수 업데이트 실패 - productId: {}, viewCount: {}, error: {}",
					data.getProductId(), data.getViewCount(), e.getMessage(), e);
				throw e; // Chunk 트랜잭션 롤백
			}
		}

		log.debug("조회수 배치 Write 완료 - 처리된 데이터: {}개", chunk.size());
	}

}
