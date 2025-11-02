package org.oneog.uppick.batch.domain.bidprice.reader;

import java.util.Iterator;
import java.util.Set;

import javax.sql.DataSource;

import org.oneog.uppick.batch.domain.bidprice.dto.BidPriceDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 입찰가 배치 ItemReader
 *
 * Redis에서 auction:*:current-bid-price 패턴의 키를 읽어 BidPriceDto로 변환
 */
@Slf4j
@Component
public class BidPriceItemReader implements ItemReader<BidPriceDto> {

	private static final String REDIS_KEY_PATTERN = "auction:*:current-bid-price";
	private static final String REDIS_KEY_PREFIX = "auction:";
	private static final String REDIS_KEY_SUFFIX = ":current-bid-price";
	private static final String QUERY_PRODUCT_ID = "SELECT product_id FROM auction WHERE id = ?";

	private final StringRedisTemplate stringRedisTemplate;
	private final JdbcTemplate auctionJdbcTemplate;

	private Iterator<String> keyIterator;

	public BidPriceItemReader(
		StringRedisTemplate stringRedisTemplate,
		@Qualifier("auctionDataSource") DataSource auctionDataSource
	) {

		this.stringRedisTemplate = stringRedisTemplate;
		this.auctionJdbcTemplate = new JdbcTemplate(auctionDataSource);
	}

	/**
	 * Reader 초기화
	 * Redis에서 auction:*:current-bid-price 패턴의 모든 키를 조회하여 Iterator 생성
	 */
	public void init() {

		Set<String> keys = stringRedisTemplate.keys(REDIS_KEY_PATTERN);

		if (keys == null || keys.isEmpty()) {
			log.info("처리할 입찰가 데이터가 없습니다.");
			keyIterator = Set.<String>of().iterator();
			return;
		}

		log.info("입찰가 배치 처리 시작 - 총 {}개의 Redis 키 발견", keys.size());
		keyIterator = keys.iterator();
	}

	@Override
	public BidPriceDto read() throws Exception {

		// 더 이상 읽을 데이터가 없으면 null 반환 (Chunk 처리 종료)
		if (keyIterator == null || !keyIterator.hasNext()) {
			return null;
		}

		String key = keyIterator.next();

		try {
			// Redis 키에서 auctionId 추출: auction:123:current-bid-price -> 123
			String auctionIdStr = key.replace(REDIS_KEY_PREFIX, "").replace(REDIS_KEY_SUFFIX, "");
			Long auctionId = Long.parseLong(auctionIdStr);

			// Redis에서 입찰가 값 가져오기
			String bidPriceStr = stringRedisTemplate.opsForValue().get(key);

			if (bidPriceStr == null) {
				log.warn("Redis 키 {}에 대한 값이 없습니다. 스킵합니다.", key);
				return read(); // 다음 키로 재귀 호출
			}

			Long bidPrice = Long.parseLong(bidPriceStr);

			// 입찰가가 0 이하면 스킵
			if (bidPrice <= 0) {
				log.debug("auctionId {}의 입찰가가 {}이므로 스킵합니다.", auctionId, bidPrice);
				return read(); // 다음 키로 재귀 호출
			}

			// auction 테이블에서 productId 조회
			Long productId = auctionJdbcTemplate.queryForObject(QUERY_PRODUCT_ID, Long.class, auctionId);

			if (productId == null) {
				log.warn("auctionId {}에 대한 productId를 찾을 수 없습니다. 스킵합니다.", auctionId);
				return read(); // 다음 키로 재귀 호출
			}

			log.debug("입찰가 데이터 읽기 완료 - auctionId: {}, productId: {}, bidPrice: {}", auctionId, productId, bidPrice);

			return new BidPriceDto(auctionId, productId, bidPrice);

		} catch (NumberFormatException e) {
			log.error("잘못된 형식의 Redis 데이터 - key: {}, error: {}", key, e.getMessage());
			return read(); // 다음 키로 재귀 호출
		}
	}

}
