package org.oneog.uppick.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 조회수 업데이트 Lambda Handler
 * EventBridge 스케줄러에 의해 주기적으로 실행됨
 *
 * Redis의 product:view:* 키를 읽어서 DB에 업데이트 후 Redis 키 삭제
 */
public class ViewCountUpdateHandler implements RequestHandler<Object, Map<String, Object>> {

	private static final String REDIS_HOST = System.getenv("REDIS_HOST");
	private static final String REDIS_PORT = System.getenv("REDIS_PORT");
	private static final String REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");

	private static final String DB_URL = System.getenv("DB_URL");
	private static final String DB_USERNAME = System.getenv("DB_USERNAME");
	private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

	private static final String REDIS_KEY_PATTERN = "product:view:*";
	private static final String REDIS_KEY_PREFIX = "product:view:";
	private static final String UPDATE_SQL = "UPDATE product SET view_count = view_count + ? WHERE id = ?";

	private static JedisPool jedisPool;
	private static HikariDataSource dataSource;

	/**
	 * Redis와 DB 연결 초기화 (싱글톤, Lazy)
	 */
	private synchronized void initializeResources() {
		if (jedisPool == null) {
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(10);
			poolConfig.setMaxIdle(5);
			poolConfig.setMinIdle(1);
			poolConfig.setTestOnBorrow(true);

			int port = REDIS_PORT != null ? Integer.parseInt(REDIS_PORT) : 6379;

			if (REDIS_PASSWORD != null && !REDIS_PASSWORD.isEmpty()) {
				jedisPool = new JedisPool(poolConfig, REDIS_HOST, port, 2000, REDIS_PASSWORD);
			} else {
				jedisPool = new JedisPool(poolConfig, REDIS_HOST, port, 2000);
			}
		}

		if (dataSource == null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(DB_URL);
			config.setUsername(DB_USERNAME);
			config.setPassword(DB_PASSWORD);
			config.setMaximumPoolSize(5);
			config.setMinimumIdle(1);
			config.setConnectionTimeout(Duration.ofSeconds(10).toMillis());
			config.setIdleTimeout(Duration.ofMinutes(5).toMillis());
			config.setMaxLifetime(Duration.ofMinutes(10).toMillis());

			dataSource = new HikariDataSource(config);
		}
	}

	@Override
	public Map<String, Object> handleRequest(Object input, Context context) {
		var logger = context.getLogger();
		Map<String, Object> response = new HashMap<>();

		int processedCount = 0;
		int failedCount = 0;

		try {
			logger.log("=== 조회수 업데이트 Lambda 시작 ===");
			logger.log("Redis: " + REDIS_HOST + ":" + REDIS_PORT);
			logger.log("DB: " + DB_URL);

			// 환경 변수 검증
			validateEnvironmentVariables();

			// 리소스 초기화 (Lazy)
			initializeResources();

			// Redis에서 product:view:* 키 조회
			Set<String> keys;
			try (Jedis jedis = jedisPool.getResource()) {
				keys = jedis.keys(REDIS_KEY_PATTERN);
			}

			if (keys == null || keys.isEmpty()) {
				logger.log("처리할 조회수 데이터가 없습니다.");
				response.put("statusCode", 200);
				response.put("message", "처리할 데이터 없음");
				response.put("success", true);
				response.put("processedCount", 0);
				return response;
			}

			logger.log("총 " + keys.size() + "개의 조회수 데이터 발견");

			// 각 키 처리
			for (String key : keys) {
				try {
					if (processViewCount(key, logger)) {
						processedCount++;
					} else {
						failedCount++;
					}
				} catch (Exception e) {
					logger.log("키 처리 실패: " + key + ", 오류: " + e.getMessage());
					failedCount++;
				}
			}

			logger.log("조회수 업데이트 완료 - 성공: " + processedCount + ", 실패: " + failedCount);

			response.put("statusCode", 200);
			response.put("message", "조회수 업데이트 완료");
			response.put("success", true);
			response.put("processedCount", processedCount);
			response.put("failedCount", failedCount);

		} catch (Exception e) {
			logger.log("예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
			response.put("statusCode", 500);
			response.put("message", "조회수 업데이트 실패: " + e.getMessage());
			response.put("success", false);
			response.put("processedCount", processedCount);
			response.put("failedCount", failedCount);
			response.put("error", e.getClass().getSimpleName());
		} finally {
			logger.log("=== 조회수 업데이트 Lambda 종료 ===");
		}

		return response;
	}

	/**
	 * 단일 조회수 데이터 처리
	 *
	 * @param key Redis 키 (product:view:{productId})
	 * @param logger Lambda 로거
	 * @return 처리 성공 여부
	 */
	private boolean processViewCount(String key, com.amazonaws.services.lambda.runtime.LambdaLogger logger) {
		try {
			// productId 추출
			Long productId = Long.parseLong(key.replace(REDIS_KEY_PREFIX, ""));

			// Redis에서 조회수 조회
			String viewCountStr;
			try (Jedis jedis = jedisPool.getResource()) {
				viewCountStr = jedis.get(key);
			}

			if (viewCountStr == null) {
				logger.log("키 " + key + "에 대한 값이 없습니다. 스킵합니다.");
				return false;
			}

			Long viewCount = Long.parseLong(viewCountStr);

			if (viewCount <= 0) {
				logger.log("productId " + productId + "의 조회수가 " + viewCount + "이므로 스킵합니다.");
				return false;
			}

			// DB 업데이트
			try (Connection conn = dataSource.getConnection();
			     PreparedStatement pstmt = conn.prepareStatement(UPDATE_SQL)) {

				pstmt.setLong(1, viewCount);
				pstmt.setLong(2, productId);

				int updatedRows = pstmt.executeUpdate();

				if (updatedRows > 0) {
					logger.log("productId " + productId + "의 조회수 " + viewCount + "만큼 증가 완료");

					// Redis 키 삭제
					try (Jedis jedis = jedisPool.getResource()) {
						jedis.del(key);
					}

					logger.log("Redis 키 삭제 완료: " + key);
					return true;
				} else {
					logger.log("productId " + productId + "에 해당하는 상품이 DB에 존재하지 않습니다.");
					return false;
				}
			}

		} catch (NumberFormatException e) {
			logger.log("잘못된 형식의 데이터 - key: " + key + ", error: " + e.getMessage());
			return false;
		} catch (SQLException e) {
			logger.log("DB 오류 발생 - key: " + key + ", error: " + e.getMessage());
			throw new RuntimeException(e);
		}
	}

	/**
	 * 환경 변수 검증
	 */
	private void validateEnvironmentVariables() {
		if (REDIS_HOST == null || REDIS_HOST.isEmpty()) {
			throw new IllegalStateException("REDIS_HOST 환경 변수가 설정되지 않았습니다");
		}
		if (DB_URL == null || DB_URL.isEmpty()) {
			throw new IllegalStateException("DB_URL 환경 변수가 설정되지 않았습니다");
		}
		if (DB_USERNAME == null || DB_USERNAME.isEmpty()) {
			throw new IllegalStateException("DB_USERNAME 환경 변수가 설정되지 않았습니다");
		}
		if (DB_PASSWORD == null || DB_PASSWORD.isEmpty()) {
			throw new IllegalStateException("DB_PASSWORD 환경 변수가 설정되지 않았습니다");
		}
	}

}
