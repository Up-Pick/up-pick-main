package org.oneog.uppick.domain.auction.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.auction.dto.request.AuctionBidRequest;
import org.oneog.uppick.domain.auction.service.AuctionInternalService;
import org.oneog.uppick.support.RestDocsBase;
import org.oneog.uppick.support.auth.WithMockAuthMember;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;

// @BeforeEach와 수동 SecurityContext 설정 대신, 클래스 레벨에 @WithMockAuthMember를 적용합니다.
@WebMvcTest(AuctionController.class)
@WithMockAuthMember(memberId = 1L) // 기본 인증 정보를 설정
class AuctionControllerRestDocsTest extends RestDocsBase {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final Long memberId = 1L; // @WithMockAuthMember의 memberId와 일치
	private final Long auctionId = 10L;
	@MockitoBean
	private AuctionInternalService auctionInternalService;

	@Test
	@DisplayName("경매 입찰 API를 Rest Docs로 문서화한다")
	void documentAuctionBid() throws Exception {
		// given
		Long biddingPrice = 120000L;
		AuctionBidRequest request = new AuctionBidRequest(biddingPrice);

		// auctionInternalService.bid() 메서드는 Void를 반환하므로 doNothing() 설정
		// @WithMockAuthMember에 설정된 memberId를 사용합니다.
		doNothing().when(auctionInternalService).bid(any(AuctionBidRequest.class), eq(auctionId), eq(memberId));

		// when & then
		mockMvc.perform(
				post("/api/v1/auctions/{auctionId}/bid", auctionId)
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("success").value(true))
			.andExpect(jsonPath("message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("data").doesNotExist()) // 응답 데이터가 null인 경우
			.andDo(
				document(
					"auction-bid", // 생성될 스니펫 디렉토리 이름
					pathParameters(
						parameterWithName("auctionId").description("입찰할 경매 ID")
					),
					requestFields(
						fieldWithPath("biddingPrice").type(JsonFieldType.NUMBER).description("사용자가 제시하는 입찰 금액")
					),
					responseFields( // GlobalApiResponse<Void>에 대한 응답 필드
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (없음)")
					)
				)
			);
	}
}
