package org.oneog.uppick.lambda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.OpenSearchException;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

/**
 * 입찰가 업데이트 Lambda Handler
 * EventBridge 스케줄러에 의해 매 1분마다 실행됨
 *
 * Redis의 opensearch:sync:* 동기화 플래그를 읽어서 OpenSearch에 업데이트 후 플래그 삭제
 */
public class BidPriceUpdateHandler implements RequestHandler<Object, Map<String, Object>> {

	// Redis 동기화 플래그 패턴
	private static final String REDIS_SYNC_PATTERN = "opensearch:sync:*";
	private static final String REDIS_SYNC_PREFIX = "opensearch:sync:";
	// SQL 쿼리
	private static final String FIND_PRODUCT_ID_SQL = "SELECT product_id FROM auction WHERE id = ?";
	// OpenSearch 인덱스 정보
	private static final String INDEX_NAME = "product";
	private static final String FIELD_CURRENT_BID_PRICE = "current_bid_price";
	// 리소스 (Warm Start 재사용)
	private static JedisPool jedisPool;
	private static HikariDataSource dataSource;
	private static OpenSearchClient openSearchClient;

	// 환경 변수 읽기 (System.getenv 또는 System.getProperty 지원)
	private static String getEnvOrProperty(String key) {

		String value = System.getenv(key);
		if (value == null) {
			value = System.getProperty(key);
		}
		return value;
	}

	// Redis 연결 정보
	private static String REDIS_HOST() {

		return getEnvOrProperty("REDIS_HOST");
	}

	private static String REDIS_PORT() {

		return getEnvOrProperty("REDIS_PORT");
	}

	private static String REDIS_PASSWORD() {

		return getEnvOrProperty("REDIS_PASSWORD");
	}

	// Auction DB 연결 정보
	private static String AUCTION_DB_URL() {

		return getEnvOrProperty("AUCTION_DB_URL");
	}

	private static String AUCTION_DB_USERNAME() {

		return getEnvOrProperty("AUCTION_DB_USERNAME");
	}

	private static String AUCTION_DB_PASSWORD() {

		return getEnvOrProperty("AUCTION_DB_PASSWORD");
	}

	// OpenSearch 연결 정보
	private static String OPENSEARCH_URL() {

		return getEnvOrProperty("OPENSEARCH_URL");
	}

	private static String OPENSEARCH_REGION() {

		return getEnvOrProperty("OPENSEARCH_REGION");
	}

	/**
	 * Redis, DB, OpenSearch 연결 초기화 (싱글톤, Lazy)
	 */
	private synchronized void initializeResources() {

		// Redis 초기화
		if (jedisPool == null) {
			JedisPoolConfig poolConfig = new JedisPoolConfig();
			poolConfig.setMaxTotal(10);
			poolConfig.setMaxIdle(5);
			poolConfig.setMinIdle(1);
			poolConfig.setTestOnBorrow(true);

			int port = REDIS_PORT() != null ? Integer.parseInt(REDIS_PORT()) : 6379;

			if (REDIS_PASSWORD() != null && !REDIS_PASSWORD().isEmpty()) {
				jedisPool = new JedisPool(poolConfig, REDIS_HOST(), port, 2000, REDIS_PASSWORD());
			} else {
				jedisPool = new JedisPool(poolConfig, REDIS_HOST(), port, 2000);
			}
		}

		// Auction DB 초기화
		if (dataSource == null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(AUCTION_DB_URL());
			config.setUsername(AUCTION_DB_USERNAME());
			config.setPassword(AUCTION_DB_PASSWORD());
			config.setMaximumPoolSize(5);
			config.setMinimumIdle(1);
			config.setConnectionTimeout(Duration.ofSeconds(10).toMillis());
			config.setIdleTimeout(Duration.ofMinutes(5).toMillis());
			config.setMaxLifetime(Duration.ofMinutes(10).toMillis());

			dataSource = new HikariDataSource(config);
		}

		// OpenSearch 초기화
		if (openSearchClient == null) {
			SdkHttpClient httpClient = ApacheHttpClient.builder().build();

			// OPENSEARCH_URL에서 https:// 제거
			String host = OPENSEARCH_URL().replaceFirst("^https?://", "");

			String region = OPENSEARCH_REGION() != null ? OPENSEARCH_REGION() : "us-east-1";

			openSearchClient = new OpenSearchClient(
				new AwsSdk2Transport(
					httpClient,
					host,
					Region.of(region),
					AwsSdk2TransportOptions.builder().build()
				)
			);
		}
	}

	@Override
	public Map<String, Object> handleRequest(Object input, Context context) {

		var logger = context.getLogger();
		Map<String, Object> response = new HashMap<>();

		int processedCount = 0;
		int failedCount = 0;

		try {
			logger.log("=== 입찰가 업데이트 Lambda 시작 ===");
			logger.log("Redis: " + REDIS_HOST() + ":" + REDIS_PORT());
			logger.log("Auction DB: " + AUCTION_DB_URL());
			logger.log("OpenSearch: " + OPENSEARCH_URL());

			// 환경 변수 검증
			validateEnvironmentVariables();

			// 리소스 초기화 (Lazy)
			initializeResources();

			// Redis에서 opensearch:sync:* 동기화 플래그 조회
			Set<String> keys;
			try (Jedis jedis = jedisPool.getResource()) {
				keys = jedis.keys(REDIS_SYNC_PATTERN);
			}

			if (keys == null || keys.isEmpty()) {
				logger.log("처리할 입찰가 데이터가 없습니다.");
				response.put("statusCode", 200);
				response.put("message", "처리할 데이터 없음");
				response.put("success", true);
				response.put("processedCount", 0);
				return response;
			}

			logger.log("총 " + keys.size() + "개의 입찰가 데이터 발견");

			// 각 키 처리
			for (String key : keys) {
				try {
					if (processBidPrice(key, logger)) {
						processedCount++;
					} else {
						failedCount++;
					}
				} catch (Exception e) {
					logger.log("키 처리 실패: " + key + ", 오류: " + e.getMessage());
					failedCount++;
				}
			}

			logger.log("입찰가 업데이트 완료 - 성공: " + processedCount + ", 실패: " + failedCount);

			response.put("statusCode", 200);
			response.put("message", "입찰가 업데이트 완료");
			response.put("success", true);
			response.put("processedCount", processedCount);
			response.put("failedCount", failedCount);

		} catch (Exception e) {
			logger.log(" 예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
			response.put("statusCode", 500);
			response.put("message", "입찰가 업데이트 실패: " + e.getMessage());
			response.put("success", false);
			response.put("processedCount", processedCount);
			response.put("failedCount", failedCount);
			response.put("error", e.getClass().getSimpleName());
		} finally {
			logger.log("=== 입찰가 업데이트 Lambda 종료 ===");
		}

		return response;
	}

	/**
	 * 단일 입찰가 데이터 처리 (동기화 플래그 기반)
	 *
	 * @param syncKey Redis 동기화 플래그 키 (opensearch:sync:{auctionId})
	 * @param logger Lambda 로거
	 * @return 처리 성공 여부
	 */
	private boolean processBidPrice(String syncKey, com.amazonaws.services.lambda.runtime.LambdaLogger logger) {

		try {
			// auctionId 추출 (opensearch:sync:123 -> 123)
			String auctionIdStr = syncKey.replace(REDIS_SYNC_PREFIX, "");
			Long auctionId = Long.parseLong(auctionIdStr);

			// Redis에서 입찰가 조회 (동기화 플래그 값)
			String bidPriceStr;
			try (Jedis jedis = jedisPool.getResource()) {
				bidPriceStr = jedis.get(syncKey);
			}

			if (bidPriceStr == null || bidPriceStr.isEmpty()) {
				logger.log("동기화 플래그 " + syncKey + "에 대한 값이 없습니다. 스킵합니다.");
				return false;
			}

			Long bidPrice = Long.parseLong(bidPriceStr);

			if (bidPrice <= 0) {
				logger.log("auctionId " + auctionId + "의 입찰가가 " + bidPrice + "이므로 스킵합니다.");
				return false;
			}

			// Auction DB에서 productId 조회
			Long productId = findProductId(auctionId);

			if (productId == null) {
				logger.log("auctionId " + auctionId + "에 해당하는 상품이 DB에 존재하지 않습니다.");
				// 동기화 플래그 삭제 (존재하지 않는 경매는 재처리 불필요)
				try (Jedis jedis = jedisPool.getResource()) {
					jedis.del(syncKey);
				}
				return false;
			}

			// OpenSearch 업데이트 (동기화 플래그가 있으면 무조건 업데이트)
			updateOpenSearch(productId, bidPrice, logger);
			logger.log("auctionId " + auctionId + ", productId " + productId + "의 입찰가 " + bidPrice + "로 업데이트 완료");

			// 동기화 완료 후 플래그 삭제
			try (Jedis jedis = jedisPool.getResource()) {
				jedis.del(syncKey);
				logger.log("동기화 플래그 삭제 완료: " + syncKey);
			}

			return true;

		} catch (NumberFormatException e) {
			logger.log("잘못된 형식의 데이터 - key: " + syncKey + ", error: " + e.getMessage());
			return false;
		} catch (Exception e) {
			logger.log("처리 오류 - key: " + syncKey + ", error: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Auction DB에서 productId 조회
	 */
	private Long findProductId(Long auctionId) throws SQLException {

		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(FIND_PRODUCT_ID_SQL)) {

			pstmt.setLong(1, auctionId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getLong("product_id");
				}
			}
		}
		return null;
	}

	/**
	 * OpenSearch product 문서의 current_bid_price 업데이트
	 */
	private void updateOpenSearch(Long productId, Long bidPrice,
		com.amazonaws.services.lambda.runtime.LambdaLogger logger) throws Exception {

		try {
			// OpenSearch UpdateRequest 생성
			Map<String, Object> doc = new HashMap<>();
			doc.put(FIELD_CURRENT_BID_PRICE, bidPrice);

			openSearchClient.update(u -> u
					.index(INDEX_NAME)
					.id(productId.toString())
					.doc(doc),
				Object.class
			);

		} catch (OpenSearchException e) {
			// 문서가 없는 경우 (인덱싱 안됨, 삭제됨 등) - 경고 로그만 남기고 무시
			if (e.getMessage() != null && e.getMessage().contains("document_missing_exception")) {
				logger.log(" OpenSearch 문서 없음 (인덱싱 안됨) - productId: " + productId + ", 입찰가 업데이트 스킵");
				return;
			}

			logger.log(" OpenSearch 업데이트 실패 - productId: " + productId + ", bidPrice: " + bidPrice
				+ ", error: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * 환경 변수 검증
	 */
	private void validateEnvironmentVariables() {

		if (REDIS_HOST() == null || REDIS_HOST().isEmpty()) {
			throw new IllegalStateException("REDIS_HOST 환경 변수가 설정되지 않았습니다");
		}
		if (AUCTION_DB_URL() == null || AUCTION_DB_URL().isEmpty()) {
			throw new IllegalStateException("AUCTION_DB_URL 환경 변수가 설정되지 않았습니다");
		}
		if (AUCTION_DB_USERNAME() == null || AUCTION_DB_USERNAME().isEmpty()) {
			throw new IllegalStateException("AUCTION_DB_USERNAME 환경 변수가 설정되지 않았습니다");
		}
		if (AUCTION_DB_PASSWORD() == null || AUCTION_DB_PASSWORD().isEmpty()) {
			throw new IllegalStateException("AUCTION_DB_PASSWORD 환경 변수가 설정되지 않았습니다");
		}
		if (OPENSEARCH_URL() == null || OPENSEARCH_URL().isEmpty()) {
			throw new IllegalStateException("OPENSEARCH_URL 환경 변수가 설정되지 않았습니다");
		}
	}

}
