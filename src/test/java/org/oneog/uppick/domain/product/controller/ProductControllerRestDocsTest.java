package org.oneog.uppick.domain.product.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.common.auth.JwtUtil;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.service.ProductInternalService;
import org.oneog.uppick.support.RestDocsBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerRestDocsTest extends RestDocsBase {

	@Autowired
	private MockMvc mockMvc;

	@MockitoBean
	private JwtUtil jwtUtil;

	@MockitoBean
	private ProductInternalService productInternalService;

	@Test
	@DisplayName("상품 상세 조회 API를 Rest Docs 로 문서화한다")
	void documentGetProductInfo() throws Exception {
		Long productId = 1L;
		LocalDateTime now = LocalDateTime.of(2024, 1, 1, 10, 0, 0);

		ProductInfoResponse response = new ProductInfoResponse(
			productId,
			"게이밍 키보드",
			"기계식 키보드 입니다.",
			42L,
			now,
			"https://example.com/products/1",
			"컴퓨터/키보드",
			now.plusDays(10),
			100000L,
			150000L,
			now.plusDays(7),
			"닉네임");

		given(productInternalService.getProductInfoById(productId)).willReturn(response);

		mockMvc.perform(
				get("/api/v1/products/{productId}", productId)
					.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("success").value(true))
			.andExpect(jsonPath("message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("data.id").value(productId))
			.andDo(
				document(
					"product-get",
					pathParameters(
						parameterWithName("productId").description("조회할 상품 ID")),
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
						fieldWithPath("data.name").type(JsonFieldType.STRING).description("상품 이름"),
						fieldWithPath("data.description").type(JsonFieldType.STRING).description("상품 설명"),
						fieldWithPath("data.viewCount").type(JsonFieldType.NUMBER).description("상품 조회수"),
						fieldWithPath("data.registeredAt").type(JsonFieldType.STRING).description("상품 등록 일시")
							.attributes(key("format").value("yyyy-MM-dd")),
						fieldWithPath("data.image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
						fieldWithPath("data.categoryName").type(JsonFieldType.STRING).description("카테고리 이름"),
						fieldWithPath("data.soldAt").type(JsonFieldType.STRING).optional()
							.description("판매 완료 날짜")
							.attributes(key("format").value("yyyy-MM-dd")),
						fieldWithPath("data.minPrice").type(JsonFieldType.NUMBER).description("최소 입찰가")
							.attributes(key("example").value("100000")),
						fieldWithPath("data.currentBid").type(JsonFieldType.NUMBER).description("현재 입찰가")
							.attributes(key("example").value("150000")),
						fieldWithPath("data.endAt").type(JsonFieldType.STRING).description("경매 종료 예정 날짜")
							.attributes(key("format").value("yyyy-MM-dd")),
						fieldWithPath("data.sellerName").type(JsonFieldType.STRING).description("판매자 닉네임"))));
	}
}
