package org.oneog.uppick.auction.domain.product.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.restdocs.snippet.Attributes.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.auction.domain.product.command.model.dto.request.ProductRegisterRequest;
import org.oneog.uppick.auction.domain.product.command.model.dto.request.SearchProductRequest;
import org.oneog.uppick.auction.domain.product.common.enums.ProductSearchSortType;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductBiddingInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductDetailResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSellingInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.PurchasedProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.SoldProductInfoResponse;
import org.oneog.uppick.auction.domain.product.service.ProductInternalService;
import org.oneog.uppick.auction.support.auth.WithMockAuthMember;
import org.oneog.uppick.auction.support.restdocs.RestDocsBase;
import org.oneog.uppick.common.dto.AuthMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
		ProductDetailResponse response = ProductDetailResponse
			.builder()
			.id(1L)
			.name("테스트 상품")
			.description("상품 설명")
			.viewCount(10L)
			.registeredAt(LocalDateTime.now())
			.image("image.jpg")
			.categoryName("전자제품")
			.minPrice(1_000L)
			.currentBid(1_500L)
			.endAt(LocalDateTime.now().plusDays(7))
			.sellerName("sellerName")
			.build();

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
					fieldWithPath("data.currentBid").type(JsonFieldType.NUMBER)
						.description("현재 입찰가")
						.attributes(key("optional").value(true)),
					fieldWithPath("data.endAt").type(JsonFieldType.STRING).description("마감 일시 (ISO-8601)"),
					fieldWithPath("data.sellerName").type(JsonFieldType.STRING).description("판매자 이름"))));
	}

	@Test
	void getProductSimpleInfo_정상적인상황_상품간단조회성공() throws Exception {

		Long productId = 1L;
		ProductSimpleInfoResponse response = ProductSimpleInfoResponse.builder()
			.name("테스트 상품")
			.image("image.jpg")
			.minBidPrice(1_000L)
			.currentBidPrice(1_500L)
			.build();

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
					fieldWithPath("data.currentBidPrice").type(JsonFieldType.NUMBER)
						.description("현재 입찰가")
						.attributes(key("optional").value(true)))));
	}

	@Test
	@WithMockAuthMember(memberId = 10L, memberNickname = "tester")
	void getSoldProducts_정상적인상황_판매완료상품내역조회성공() throws Exception {

		List<SoldProductInfoResponse> contents = List.of(
			SoldProductInfoResponse.builder()
				.id(1L)
				.name("상품1")
				.description("상품 설명1")
				.image("image1.jpg")
				.finalPrice(30_000L)
				.soldAt(LocalDateTime.now())
				.build(),
			SoldProductInfoResponse.builder()
				.id(2L)
				.name("상품2")
				.description("상품 설명2")
				.image("image2.jpg")
				.finalPrice(45_000L)
				.soldAt(LocalDateTime.now())
				.build());

		Page<SoldProductInfoResponse> page = new PageImpl<>(contents, Pageable.ofSize(20), 2);

		given(productInternalService.getSoldProductInfosByMemberId(eq(10L), any(Pageable.class)))
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

		List<PurchasedProductInfoResponse> contents = List.of(
			PurchasedProductInfoResponse.builder()
				.id(1L)
				.name("상품1")
				.image("image1.jpg")
				.finalPrice(10_000L)
				.buyAt(LocalDateTime.now())
				.build(),
			PurchasedProductInfoResponse.builder()
				.id(2L)
				.name("상품2")
				.image("image2.jpg")
				.finalPrice(15_000L)
				.buyAt(LocalDateTime.now())
				.build());

		Page<PurchasedProductInfoResponse> page = new PageImpl<>(contents, Pageable.ofSize(20), 2);

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
			ProductBiddingInfoResponse.builder()
				.id(1L)
				.name("상품1")
				.image("image1.jpg")
				.endAt(LocalDateTime.now().plusDays(1))
				.currentBid(1_000L)
				.bidPrice(1_100L)
				.build(),
			ProductBiddingInfoResponse.builder()
				.id(2L)
				.name("상품2")
				.image("image2.jpg")
				.endAt(LocalDateTime.now().plusDays(2))
				.currentBid(2_000L)
				.bidPrice(2_100L)
				.build());
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
			ProductSellingInfoResponse.builder()
				.id(1L)
				.name("상품1")
				.image("image1.jpg")
				.endAt(LocalDateTime.now().plusDays(1))
				.currentBid(1_000L)
				.auctionId(1L)
				.build(),
			ProductSellingInfoResponse.builder()
				.id(1L)
				.name("상품2")
				.image("image2.jpg")
				.endAt(LocalDateTime.now().plusDays(2))
				.currentBid(2_000L)
				.auctionId(2L)
				.build());
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
	void searchProduct_검색시도_상품검색성공() throws Exception {

		// given
		SearchProductRequest request = SearchProductRequest.builder()
			.keyword("phone")
			.categoryId(2L)
			.onlyNotSold(true)
			.sortBy(ProductSearchSortType.REGISTERED_AT_DESC)
			.page(0)
			.size(10)
			.endAtFrom(LocalDate.of(2025, 12, 31))
			.build();

		SearchProductInfoResponse item = SearchProductInfoResponse.builder()
			.id(1L)
			.image("/images/phone.jpg")
			.name("Smart Phone")
			.registeredAt(LocalDateTime.of(2025, 1, 1, 10, 0, 0))
			.endAt(LocalDateTime.of(2025, 12, 31, 23, 59, 59))
			.currentBidPrice(50000L)
			.minBidPrice(10000L)
			.isSold(false)
			.build();

		Page<SearchProductInfoResponse> page = new PageImpl<>(List.of(item), PageRequest.of(0, 10), 1);

		given(productInternalService.searchProduct(any(SearchProductRequest.class))).willReturn(page);

		// when & then
		mockMvc.perform(
				post("/api/v1/products/search")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("success").value(true))
			.andExpect(jsonPath("message").value("요청에 성공했습니다."))
			.andExpect(jsonPath("data.page").value(0))
			.andExpect(jsonPath("data.size").value(10))
			.andExpect(jsonPath("data.totalPages").value(1))
			.andExpect(jsonPath("data.totalElements").value(1))
			.andExpect(jsonPath("data.contents[0].id").value(1))
			.andDo(
				document(
					"product-search",
					requestFields(
						fieldWithPath("keyword").type(JsonFieldType.STRING)
							.description("검색 키워드 (공백으로 구분된 다중 키워드 가능)")
							.optional(),
						fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID").optional(),
						fieldWithPath("onlyNotSold").type(JsonFieldType.BOOLEAN)
							.description("판매중인 상품만 조회할지 여부")
							.optional(),
						fieldWithPath("sortBy").type(JsonFieldType.STRING)
							.description("정렬 기준 (예: endAt, registeredAt)")
							.optional(),
						fieldWithPath("page").type(JsonFieldType.NUMBER).description("페이지 번호 (0부터)").optional(),
						fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 사이즈").optional(),
						fieldWithPath("endAtFrom").type(JsonFieldType.STRING)
							.description("마감일 필터 (yyyy-MM-dd 형식)")
							.optional()),
					responseFields(
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data.page").type(JsonFieldType.NUMBER).description("응답 페이지 번호"),
						fieldWithPath("data.size").type(JsonFieldType.NUMBER).description("응답 페이지 사이즈"),
						fieldWithPath("data.totalPages").type(JsonFieldType.NUMBER).description("총 페이지 수"),
						fieldWithPath("data.totalElements").type(JsonFieldType.NUMBER).description("총 요소 수"),
						fieldWithPath("data.contents[].id").type(JsonFieldType.NUMBER).description("상품 ID"),
						fieldWithPath("data.contents[].image").type(JsonFieldType.STRING).description("상품 이미지 경로"),
						fieldWithPath("data.contents[].name").type(JsonFieldType.STRING).description("상품명"),
						fieldWithPath("data.contents[].registeredAt").type(JsonFieldType.STRING).description("등록일시"),
						fieldWithPath("data.contents[].endAt").type(JsonFieldType.STRING).description("종료일시"),
						fieldWithPath("data.contents[].currentBidPrice").type(JsonFieldType.NUMBER)
							.description("현재 입찰가 (없으면 null)"),
						fieldWithPath("data.contents[].minBidPrice").type(JsonFieldType.NUMBER).description("최소 입찰가"),
						fieldWithPath("data.contents[].sold").type(JsonFieldType.BOOLEAN).description("판매 여부"))));
	}

}
