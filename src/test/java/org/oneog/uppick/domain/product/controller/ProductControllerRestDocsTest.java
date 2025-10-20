package org.oneog.uppick.domain.product.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.service.ProductInternalService;
import org.oneog.uppick.support.RestDocsBase;
import org.oneog.uppick.support.auth.WithMockAuthMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProductController.class)
class ProductControllerRestDocsTest extends RestDocsBase {

	@MockitoBean
	private ProductInternalService productInternalService;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void registerProduct_정상적인입력_상품등록성공() throws Exception {
		ProductRegisterRequest request = ProductRegisterRequest.builder()
			.name("테스트 상품")
			.description("상품 설명")
			.categoryId(1L)
			.startBid(1000L)
			.endAt(LocalDateTime.now().plusDays(7))
			.build();

		MockMultipartFile productPart = new MockMultipartFile(
			"product", "product", "application/json",
			objectMapper.writeValueAsBytes(request));

		MockMultipartFile imagePart = new MockMultipartFile(
			"image", "image.jpg", "image/jpeg",
			"dummy-image-content".getBytes());

		willDoNothing().given(productInternalService)
			.registerProduct(any(ProductRegisterRequest.class), any(MultipartFile.class), eq(10L));

		this.mockMvc.perform(multipart("/api/v1/products")
			.file(productPart)
			.file(imagePart)
			.header("Authorization", "Bearer token")
			.contentType(MediaType.MULTIPART_FORM_DATA)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-register-product",
				requestHeaders(
					headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")),
				requestParts(
					partWithName("product").description("상품 등록 요청 JSON"),
					partWithName("image").description("상품 대표 이미지 파일")),
				requestPartFields("product",
					fieldWithPath("name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("description").type(JsonFieldType.STRING).description("상품 설명"),
					fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
					fieldWithPath("startBid").type(JsonFieldType.NUMBER).description("시작 입찰가"),
					fieldWithPath("endAt").type(JsonFieldType.STRING).description("마감 일시 (ISO-8601)")),
				responseFields(
					fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
					fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
					fieldWithPath("data").type(JsonFieldType.NULL).description("응답 데이터 (여기서는 null)"))));

	}

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void getProductInfo_정상적인상황_상품상세조회성공() throws Exception {
		Long productId = 1L;
		ProductInfoResponse response = new ProductInfoResponse(
			1L, "테스트 상품", "상품 설명", 10L, LocalDateTime.now(), "image.jpg",
			"전자제품", 1000L, 1500L, LocalDateTime.now().plusDays(7), "판매자");

		given(productInternalService.getProductInfoById(eq(productId), any(AuthMember.class)))
			.willReturn(response);

		this.mockMvc.perform(get("/api/v1/products/{productId}", productId)
			.header("Authorization", "Bearer token")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-get-product-info",
				requestHeaders(
					headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")
						.attributes(key("optional").value(true))),
				pathParameters(
					parameterWithName("productId").description("상품 ID")),
				responseFields(
					fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
					fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
					fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
					fieldWithPath("data.name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("data.description").type(JsonFieldType.STRING).description("상품 설명"),
					fieldWithPath("data.viewCount").type(JsonFieldType.NUMBER).description("조회수"),
					fieldWithPath("data.registeredAt").type(JsonFieldType.STRING).description("등록 일시 (ISO-8601)"),
					fieldWithPath("data.image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
					fieldWithPath("data.categoryName").type(JsonFieldType.STRING).description("카테고리 이름"),
					fieldWithPath("data.minPrice").type(JsonFieldType.NUMBER).description("최소 가격"),
					fieldWithPath("data.currentBid").type(JsonFieldType.NUMBER).description("현재 입찰가")
						.attributes(key("optional").value(true)),
					fieldWithPath("data.endAt").type(JsonFieldType.STRING).description("마감 일시 (ISO-8601)"),
					fieldWithPath("data.sellerName").type(JsonFieldType.STRING).description("판매자 이름"))));
	}
}
