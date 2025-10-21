package org.oneog.uppick.domain.ranking.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.ranking.dto.response.HotDealResponse;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.service.RankingInternalService;
import org.oneog.uppick.support.RestDocsBase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(RankingController.class)
public class RankingControllerRestDocsTest extends RestDocsBase {

	@MockitoBean
	private RankingInternalService rankingInternalService;

	// 핫딜 조회
	@Test
	void getHotDeals_정상적인상황_핫딜조회성공() throws Exception {
		List<HotDealResponse> responses = List.of(
			createResponse(1, 1L, "맥북 프로", "https://example.com/images/macbook.jpg"),
			createResponse(2, 2L, "아이폰 15", "https://example.com/images/iphone.jpg"),
			createResponse(3, 3L, "에어팟", "https://example.com/images/airpods.jpg"),
			createResponse(4, 4L, "갤럭시 워치", "https://example.com/images/galaxy-watch.jpg"),
			createResponse(5, 5L, "아이패드", "https://example.com/images/ipad.jpg"),
			createResponse(6, 6L, "다이슨 청소기", "https://example.com/images/dyson.jpg"));

		given(rankingInternalService.getHotDeals()).willReturn(responses);

		mockMvc.perform(
				get("/api/v1/rankings/hot-deals")
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("success").value(true))
			.andExpect(jsonPath("message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("data").isArray())
			.andDo(
				document(
					"ranking-getHotDeals",
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("핫딜 목록"),
						fieldWithPath("data[].rankNo").type(JsonFieldType.NUMBER).description("순위"),
						fieldWithPath("data[].productId").type(JsonFieldType.NUMBER).description("상품 아이디"),
						fieldWithPath("data[].productName").type(JsonFieldType.STRING).description("상품 제목"),
						fieldWithPath("data[].productImage").type(JsonFieldType.STRING).description("상품 이미지 경로"))));
	}

	//핫키워드 조회
	@Test
	void getHotKeywords_정상적인상황_핫키워드조회성공() throws Exception {
		List<HotKeywordResponse> responses = List.of(
			new HotKeywordResponse("유영관", 1),
			new HotKeywordResponse("정직한", 2),
			new HotKeywordResponse("김석준", 3),
			new HotKeywordResponse("백도현", 4),
			new HotKeywordResponse("박수현", 5),
			new HotKeywordResponse("김선용", 6),
			new HotKeywordResponse("박성규", 7),
			new HotKeywordResponse("안형욱", 8),
			new HotKeywordResponse("현석훈", 9),
			new HotKeywordResponse("최원빈", 10)
		);

		given(rankingInternalService.getHotKeywords()).willReturn(responses);

		mockMvc.perform(
				get("/api/v1/rankings/search-keywords")
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("success").value(true))
			.andExpect(jsonPath("message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("data").isArray())
			.andDo(
				document(
					"ranking-getHotKeywords",
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data[]").type(JsonFieldType.ARRAY).description("인기 검색어 목록"),
						fieldWithPath("data[].keyword").type(JsonFieldType.STRING).description("검색어"),
						fieldWithPath("data[].rankNo").type(JsonFieldType.NUMBER).description("순위"))));
	}

	// === Helper Methods ===

	private HotDealResponse createResponse(int rank, Long productId, String name, String image) {
		return HotDealResponse.builder()
			.rankNo(rank)
			.productId(productId)
			.productName(name)
			.productImage(image)
			.build();
	}
}

