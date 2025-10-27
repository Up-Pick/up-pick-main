package org.oneog.uppick.product.domain.auction.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.product.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.product.domain.auction.service.AuctionService;
import org.oneog.uppick.product.support.auth.WithMockAuthMember;
import org.oneog.uppick.product.support.restdocs.RestDocsBase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuctionController.class)
@WithMockAuthMember(memberId = 1L)
class AuctionControllerRestDocsTest extends RestDocsBase {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Long memberId = 1L;
	private final Long auctionId = 10L;
	@MockitoBean
	private AuctionService auctionService;

	@Test
	@DisplayName("경매 입찰 API를 Rest Docs로 문서화한다")
	void auctionBid_정상적인입력_입찰성공() throws Exception {
		// given
		Long biddingPrice = 120000L;
		AuctionBidRequest request = new AuctionBidRequest(biddingPrice);

		doNothing().when(auctionService).bid(any(AuctionBidRequest.class), eq(auctionId), eq(memberId));

		// when & then
		mockMvc.perform(
				post("/api/v1/auctions/{auctionId}/bid", auctionId)
					.header("Authorization", "Bearer token")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("success").value(true))
			.andExpect(jsonPath("message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("data").doesNotExist())
			.andDo(
				document(
					"auction-bid", // 생성될 스니펫 디렉토리 이름
					requestHeaders(
						headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")),
					pathParameters(
						parameterWithName("auctionId").description("입찰할 경매 ID")),
					requestFields(
						fieldWithPath("biddingPrice").type(JsonFieldType.NUMBER).description("사용자가 제시하는 입찰 금액")),
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)"))));
	}
}
