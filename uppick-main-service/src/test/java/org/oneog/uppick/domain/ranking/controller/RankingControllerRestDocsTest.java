package org.oneog.uppick.domain.ranking.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.ranking.dto.response.HotKeywordResponse;
import org.oneog.uppick.domain.ranking.service.RankingService;
import org.oneog.uppick.support.restdocs.RestDocsBase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(RankingController.class)
public class RankingControllerRestDocsTest extends RestDocsBase {

	@MockitoBean
	private RankingService rankingInternalService;

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
			new HotKeywordResponse("최원빈", 10));

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

}
