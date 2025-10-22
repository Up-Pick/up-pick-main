package org.oneog.uppick.domain.product.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductPurchasedInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductRecentViewInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.oneog.uppick.domain.product.service.ProductInternalService;
import org.oneog.uppick.support.auth.WithMockAuthMember;
import org.oneog.uppick.support.restdocs.RestDocsBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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

	@Test
	void getProductSimpleInfo_정상적인상황_상품간단조회성공() throws Exception {
		Long productId = 1L;
		ProductSimpleInfoResponse response = new ProductSimpleInfoResponse(
			"테스트 상품", "image.jpg", 1000L, 1500L);

		given(productInternalService.getProductSimpleInfoById(eq(productId)))
			.willReturn(response);

		this.mockMvc.perform(get("/api/v1/products/{productId}/simple-info", productId)
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-get-product-simple-info",
				pathParameters(
					parameterWithName("productId").description("상품 ID")),
				responseFields(
					fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
					fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
					fieldWithPath("data.name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("data.image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
					fieldWithPath("data.minBidPrice").type(JsonFieldType.NUMBER).description("최소 입찰가"),
					fieldWithPath("data.currentBidPrice").type(JsonFieldType.NUMBER).description("현재 입찰가")
						.attributes(key("optional").value(true)))));
	}

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void getSoldProducts_정상적인상황_판매완료상품내역조회성공() throws Exception {
		List<ProductSoldInfoResponse> contents = List.of(
			new ProductSoldInfoResponse(1L, "상품1", "상품 설명1", "image1.jpg", 30_000L, LocalDateTime.now()),
			new ProductSoldInfoResponse(2L, "상품2", "상품 설명2", "image2.jpg", 45_000L, LocalDateTime.now()));
		Page<ProductSoldInfoResponse> page = new PageImpl<>(contents, Pageable.ofSize(20), 2);

		given(productInternalService.getProductSoldInfoByMemberId(eq(10L), any(Pageable.class)))
			.willReturn(page);

		this.mockMvc.perform(get("/api/v1/products/sold/me")
			.header("Authorization", "Bearer token")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-get-sold-products",
				requestHeaders(
					headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")),
				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 크기"),
					fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
					fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("총 요소 수"),
					fieldWithPath("contents[]").type(JsonFieldType.ARRAY).description("판매 완료 상품 목록"),
					fieldWithPath("contents[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
					fieldWithPath("contents[].name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("contents[].description").type(JsonFieldType.STRING).description("상품 설명"),
					fieldWithPath("contents[].image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
					fieldWithPath("contents[].finalPrice").type(JsonFieldType.NUMBER).description("최종 낙찰가 가격"),
					fieldWithPath("contents[].soldAt").type(JsonFieldType.STRING).description("구매 일시 (ISO-8601)"))));
	}

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void getPurchasedProducts_정상적인상황_구매완료상품내역조회성공() throws Exception {
		List<ProductPurchasedInfoResponse> contents = List.of(
			new ProductPurchasedInfoResponse(1L, "테스트 상품1", "image1.jpg", 1000L, LocalDateTime.now()),
			new ProductPurchasedInfoResponse(2L, "테스트 상품2", "image2.jpg", 2000L, LocalDateTime.now()));
		Page<ProductPurchasedInfoResponse> page = new PageImpl<>(contents, Pageable.ofSize(20), 2);

		given(productInternalService.getPurchasedProductInfoByMemberId(eq(10L), any(Pageable.class)))
			.willReturn(page);

		this.mockMvc.perform(get("/api/v1/products/purchased/me")
			.header("Authorization", "Bearer token")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-get-purchased-products",
				requestHeaders(
					headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")),
				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 크기"),
					fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
					fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("총 요소 수"),
					fieldWithPath("contents[]").type(JsonFieldType.ARRAY).description("구매 완료 상품 목록"),
					fieldWithPath("contents[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
					fieldWithPath("contents[].name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("contents[].image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
					fieldWithPath("contents[].finalPrice").type(JsonFieldType.NUMBER).description("최종 구매 가격"),
					fieldWithPath("contents[].buyAt").type(JsonFieldType.STRING).description("구매 일시 (ISO-8601)"))));
	}

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void getBiddingProducts_정상적인상황_입찰중인상품목록조회성공() throws Exception {
		List<ProductBiddingInfoResponse> contents = List.of(
			new ProductBiddingInfoResponse(1L, "상품1", "image1.jpg", LocalDateTime.now().plusDays(1), 1000L, 1100L),
			new ProductBiddingInfoResponse(2L, "상품2", "image2.jpg", LocalDateTime.now().plusDays(2), 2000L, 2100L));
		Page<ProductBiddingInfoResponse> page = new PageImpl<>(contents, Pageable.ofSize(10), 2);

		given(productInternalService.getBiddingProductInfoByMemberId(eq(10L), any(Pageable.class)))
			.willReturn(page);

		this.mockMvc.perform(get("/api/v1/products/bidding/me")
			.header("Authorization", "Bearer token")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-get-bidding-products",
				requestHeaders(
					headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")),
				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 크기"),
					fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
					fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("총 요소 수"),
					fieldWithPath("contents[]").type(JsonFieldType.ARRAY).description("상품 목록"),
					fieldWithPath("contents[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
					fieldWithPath("contents[].name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("contents[].image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
					fieldWithPath("contents[].endAt").type(JsonFieldType.STRING).description("마감 일시 (ISO-8601)"),
					fieldWithPath("contents[].currentBid").type(JsonFieldType.NUMBER).description("현재 입찰가"),
					fieldWithPath("contents[].bidPrice").type(JsonFieldType.NUMBER).description("입찰 가격"))));
	}

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void getSellingProducts_정상적인상황_경매중인상품목록조회성공() throws Exception {
		List<ProductSellingInfoResponse> contents = List.of(
			new ProductSellingInfoResponse(1L, "상품1", "image1.jpg", LocalDateTime.now().plusDays(1), 1000L, 1L),
			new ProductSellingInfoResponse(2L, "상품2", "image2.jpg", LocalDateTime.now().plusDays(2), 2000L, 2L));
		Page<ProductSellingInfoResponse> page = new PageImpl<>(contents, Pageable.ofSize(10), 2);

		given(productInternalService.getSellingProductInfoByMemberId(eq(10L), any(Pageable.class)))
			.willReturn(page);

		this.mockMvc.perform(get("/api/v1/products/selling/me")
			.header("Authorization", "Bearer token")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-get-bidding-products",
				requestHeaders(
					headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")),
				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호"),
					fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 크기"),
					fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
					fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("총 요소 수"),
					fieldWithPath("contents[]").type(JsonFieldType.ARRAY).description("상품 목록"),
					fieldWithPath("contents[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
					fieldWithPath("contents[].name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("contents[].image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
					fieldWithPath("contents[].endAt").type(JsonFieldType.STRING).description("마감 일시 (ISO-8601)"),
					fieldWithPath("contents[].currentBid").type(JsonFieldType.NUMBER).description("현재 입찰가"),
					fieldWithPath("contents[].auctionId").type(JsonFieldType.NUMBER).description("경매 ID"))));
	}

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void getRecentlyViewedProducts_정상적인상황_최근본상품조회성공() throws Exception {
		ProductRecentViewInfoResponse response1 = new ProductRecentViewInfoResponse(
			1L, "테스트 상품1", "image1.jpg", 1500L, LocalDateTime.now().plusDays(7), LocalDateTime.now().minusHours(1));
		ProductRecentViewInfoResponse response2 = new ProductRecentViewInfoResponse(
			2L, "테스트 상품2", "image2.jpg", 2000L, LocalDateTime.now().plusDays(5), LocalDateTime.now().minusHours(2));

		Page<ProductRecentViewInfoResponse> pageResponse = new PageImpl<>(List.of(response1, response2));

		given(productInternalService.getRecentViewProductInfoByMemberId(eq(10L), any(Pageable.class)))
			.willReturn(pageResponse);

		this.mockMvc.perform(get("/api/v1/products/recently-viewed/me")
			.header("Authorization", "Bearer token")
			.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andDo(document("product-get-recently-viewed-products",
				requestHeaders(
					headerWithName("Authorization").description("JWT 액세스 토큰 (Bearer {token})")),
				responseFields(
					fieldWithPath("page").type(JsonFieldType.NUMBER).description("현재 페이지 번호 (0부터 시작)"),
					fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 크기"),
					fieldWithPath("totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
					fieldWithPath("totalElements").type(JsonFieldType.NUMBER).description("총 요소 수"),
					fieldWithPath("contents[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
					fieldWithPath("contents[].name").type(JsonFieldType.STRING).description("상품 이름"),
					fieldWithPath("contents[].image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
					fieldWithPath("contents[].currentBid").type(JsonFieldType.NUMBER).description("현재 입찰가")
						.attributes(key("optional").value(true)),
					fieldWithPath("contents[].endAt").type(JsonFieldType.STRING).description("마감 일시 (ISO-8601)"),
					fieldWithPath("contents[].viewedAt").type(JsonFieldType.STRING).description("조회 일시 (ISO-8601)"))));
	}
}
