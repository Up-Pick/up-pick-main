package org.oneog.uppick.batch.domain.productviewcount.reader;

import java.util.Iterator;
import java.util.Set;

import org.oneog.uppick.batch.domain.productviewcount.dto.ViewCountDto;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 조회수 배치 ItemReader
 *
 * Redis에서 product:view:* 패턴의 키를 읽어 ViewCountData로 변환
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountItemReader implements ItemReader<ViewCountDto> {

	private static final String REDIS_KEY_PATTERN = "product:view:*";
	private static final String REDIS_KEY_PREFIX = "product:view:";

	private final StringRedisTemplate stringRedisTemplate;

	private Iterator<String> keyIterator;

	/**
	 * Reader 초기화
	 * Redis에서 product:view:* 패턴의 모든 키를 조회하여 Iterator 생성
	 */
	public void init() {

		Set<String> keys = stringRedisTemplate.keys(REDIS_KEY_PATTERN);

		if (keys == null || keys.isEmpty()) {
			log.info("처리할 조회수 데이터가 없습니다.");
			keyIterator = Set.<String>of().iterator();
			return;
		}

		log.info("조회수 배치 처리 시작 - 총 {}개의 Redis 키 발견", keys.size());
		keyIterator = keys.iterator();
	}

	@Override
	public ViewCountDto read() throws Exception {

		// 더 이상 읽을 데이터가 없으면 null 반환 (Chunk 처리 종료)
		if (keyIterator == null || !keyIterator.hasNext()) {
			return null;
		}

		String key = keyIterator.next();

		try {
			// Redis 키에서 productId 추출: product:view:123 -> 123
			Long productId = Long.parseLong(key.replace(REDIS_KEY_PREFIX, ""));

			// Redis에서 조회수 값 가져오기
			String viewCountStr = stringRedisTemplate.opsForValue().get(key);

			if (viewCountStr == null) {
				log.warn("Redis 키 {}에 대한 값이 없습니다. 스킵합니다.", key);
				return read(); // 다음 키로 재귀 호출
			}

			Long viewCount = Long.parseLong(viewCountStr);

			// 조회수가 0 이하면 스킵
			if (viewCount <= 0) {
				log.debug("productId {}의 조회수가 {}이므로 스킵합니다.", productId, viewCount);
				return read(); // 다음 키로 재귀 호출
			}

			log.debug("조회수 데이터 읽기 완료 - productId: {}, viewCount: {}", productId, viewCount);

			return new ViewCountDto(productId, viewCount);

		} catch (NumberFormatException e) {
			log.error("잘못된 형식의 Redis 데이터 - key: {}, error: {}", key, e.getMessage());
			return read(); // 다음 키로 재귀 호출
		}
	}

}
