package org.oneog.uppick.lambda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 랭킹 핫 키워드 업데이트 Lambda Handler
 * EventBridge 스케줄러에 의해 매일 자정(KST)에 실행됨
 *
 * Main DB에 직접 접근하여 핫 키워드 랭킹 업데이트 및 Redis 캐시 무효화
 */
public class RankingUpdateHandler implements RequestHandler<Object, Map<String, Object>> {

	// 환경 변수 읽기 (System.getenv 또는 System.getProperty 지원)
	private static String getEnvOrProperty(String key) {
		String value = System.getenv(key);
		if (value == null) {
			value = System.getProperty(key);
		}
		return value;
	}

	// Main DB 연결 정보 (lazy evaluation을 위해 final 제거)
	private static String DB_URL() { return getEnvOrProperty("DB_URL"); }
	private static String DB_USERNAME() { return getEnvOrProperty("DB_USERNAME"); }
	private static String DB_PASSWORD() { return getEnvOrProperty("DB_PASSWORD"); }

	// Redis 연결 정보 (lazy evaluation을 위해 final 제거)
	private static String REDIS_HOST() { return getEnvOrProperty("REDIS_HOST"); }
	private static String REDIS_PORT() { return getEnvOrProperty("REDIS_PORT"); }
	private static String REDIS_PASSWORD() { return getEnvOrProperty("REDIS_PASSWORD"); }

	// SQL 쿼리
	private static final String DELETE_ALL_SQL = "DELETE FROM hot_keyword";
	private static final String INSERT_SQL = "INSERT INTO hot_keyword (keyword, rank_no) VALUES (?, ?)";
	private static final String FIND_TOP10_SQL =
		"SELECT keyword, COUNT(*) as count " +
			"FROM search_history " +
			"WHERE searched_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
			"GROUP BY keyword " +
			"ORDER BY count DESC " +
			"LIMIT 10";

	// Redis 캐시 키
	private static final String CACHE_KEY = "hotKeywords::top10";

	private static HikariDataSource dataSource;
	private static JedisPool jedisPool;

	public RankingUpdateHandler() {
		// Lazy initialization - 환경 변수가 설정된 후에 초기화됨
	}

	/**
	 * 리소스 정리 (Lambda 종료 시)
	 */
	public static void cleanup() {

		if (dataSource != null && !dataSource.isClosed()) {
			dataSource.close();
		}
		if (jedisPool != null && !jedisPool.isClosed()) {
			jedisPool.close();
		}
	}

	/**
	 * DB와 Redis 연결 초기화 (싱글톤)
	 */
	private synchronized void initializeResources() {

		if (dataSource == null) {
			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(DB_URL());
			config.setUsername(DB_USERNAME());
			config.setPassword(DB_PASSWORD());
			config.setMaximumPoolSize(5);
			config.setMinimumIdle(1);
			config.setConnectionTimeout(Duration.ofSeconds(10).toMillis());
			config.setIdleTimeout(Duration.ofMinutes(5).toMillis());
			config.setMaxLifetime(Duration.ofMinutes(10).toMillis());

			dataSource = new HikariDataSource(config);
		}

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
	}

	@Override
	public Map<String, Object> handleRequest(Object input, Context context) {

		var logger = context.getLogger();
		Map<String, Object> response = new HashMap<>();

		try {
			logger.log("=== 랭킹 업데이트 Lambda 시작 ===");
			logger.log("DB: " + DB_URL());
			logger.log("Redis: " + REDIS_HOST() + ":" + REDIS_PORT());

			// 환경 변수 검증
			validateEnvironmentVariables();

			// 리소스 초기화 (Lazy)
			initializeResources();

			// 1. 기존 핫 키워드 삭제
			logger.log(" 기존 핫 키워드 데이터 삭제 중...");
			deleteAllHotKeywords(logger);

			// 2. Top 10 핫 키워드 조회 및 저장
			logger.log("최근 7일간 Top 10 핫 키워드 조회 및 저장 중...");
			List<HotKeywordData> keywords = findTop10HotKeywords(logger);

			if (keywords.isEmpty()) {
				logger.log(" 핫 키워드 데이터가 없습니다. (검색 이력이 없음)");
			} else {
				// 랭킹 번호와 함께 저장
				for (int i = 0; i < keywords.size(); i++) {
					HotKeywordData data = keywords.get(i);
					insertHotKeyword(data.keyword, i + 1, logger);
					logger.log("  - Rank " + (i + 1) + ": " + data.keyword + " (검색 횟수: " + data.count + ")");
				}
			}

			// 3. Redis 캐시 무효화
			logger.log(" Redis 캐시 무효화 중...");
			evictCache(logger);

			logger.log(" 랭킹 업데이트 완료! (총 " + keywords.size() + "개 키워드)");

			response.put("statusCode", 200);
			response.put("message", "랭킹 업데이트 성공");
			response.put("success", true);
			response.put("keywordCount", keywords.size());

		} catch (SQLException e) {
			logger.log("DB 오류 발생: " + e.getMessage());
			e.printStackTrace();
			response.put("statusCode", 500);
			response.put("message", "DB 오류: " + e.getMessage());
			response.put("success", false);
			response.put("error", e.getClass().getSimpleName());
		} catch (Exception e) {
			logger.log(" 예상치 못한 오류 발생: " + e.getMessage());
			e.printStackTrace();
			response.put("statusCode", 500);
			response.put("message", "예상치 못한 오류: " + e.getMessage());
			response.put("success", false);
			response.put("error", e.getClass().getSimpleName());
		} finally {
			logger.log("=== 랭킹 업데이트 Lambda 종료 ===");
		}

		return response;
	}

	/**
	 * 기존 핫 키워드 전체 삭제
	 */
	private void deleteAllHotKeywords(com.amazonaws.services.lambda.runtime.LambdaLogger logger) throws SQLException {

		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(DELETE_ALL_SQL)) {

			int deletedRows = pstmt.executeUpdate();
			logger.log("   기존 핫 키워드 " + deletedRows + "개 삭제 완료");
		}
	}

	/**
	 * 최근 7일간 Top 10 핫 키워드 조회
	 */
	private List<HotKeywordData> findTop10HotKeywords(com.amazonaws.services.lambda.runtime.LambdaLogger logger) throws
		SQLException {

		List<HotKeywordData> keywords = new ArrayList<>();

		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(FIND_TOP10_SQL);
			 ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				String keyword = rs.getString("keyword");
				long count = rs.getLong("count");
				keywords.add(new HotKeywordData(keyword, count));
			}

			logger.log("  Top 10 핫 키워드 조회 완료 (총 " + keywords.size() + "개)");
		}

		return keywords;
	}

	/**
	 * 핫 키워드 저장
	 */
	private void insertHotKeyword(String keyword, int rankNo,
		com.amazonaws.services.lambda.runtime.LambdaLogger logger) throws SQLException {

		try (Connection conn = dataSource.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(INSERT_SQL)) {

			pstmt.setString(1, keyword);
			pstmt.setInt(2, rankNo);
			pstmt.executeUpdate();
		}
	}

	/**
	 * Redis 캐시 무효화
	 */
	private void evictCache(com.amazonaws.services.lambda.runtime.LambdaLogger logger) {

		try (Jedis jedis = jedisPool.getResource()) {
			Long deleted = jedis.del(CACHE_KEY);
			if (deleted > 0) {
				logger.log("   Redis 캐시 키 삭제 완료: " + CACHE_KEY);
			} else {
				logger.log("   삭제할 캐시 키가 없음: " + CACHE_KEY);
			}
		} catch (Exception e) {
			logger.log("  Redis 캐시 무효화 실패 (무시하고 계속): " + e.getMessage());
			// 캐시 무효화 실패는 치명적이지 않으므로 예외를 던지지 않음
		}
	}

	/**
	 * 환경 변수 검증
	 */
	private void validateEnvironmentVariables() {

		if (DB_URL() == null || DB_URL().isEmpty()) {
			throw new IllegalStateException("DB_URL 환경 변수가 설정되지 않았습니다");
		}
		if (DB_USERNAME() == null || DB_USERNAME().isEmpty()) {
			throw new IllegalStateException("DB_USERNAME 환경 변수가 설정되지 않았습니다");
		}
		if (DB_PASSWORD() == null || DB_PASSWORD().isEmpty()) {
			throw new IllegalStateException("DB_PASSWORD 환경 변수가 설정되지 않았습니다");
		}
		if (REDIS_HOST() == null || REDIS_HOST().isEmpty()) {
			throw new IllegalStateException("REDIS_HOST 환경 변수가 설정되지 않았습니다");
		}
	}

	/**
	 * 핫 키워드 데이터 클래스
	 */
	private static class HotKeywordData {

		final String keyword;
		final long count;

		HotKeywordData(String keyword, long count) {

			this.keyword = keyword;
			this.count = count;
		}

	}

}
