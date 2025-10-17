package org.oneog.uppick.domain.product.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.service.ProductInternalService;
import org.oneog.uppick.support.RestDocsConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs(outputDir = "build/generated-snippets")
@Import({ProductControllerRestDocsTest.MockConfig.class, RestDocsConfig.class})
class ProductControllerRestDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
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
            150000L,
            now.plusDays(7),
            "닉네임"
        );

        given(productInternalService.getProductInfoById(productId)).willReturn(response);

        mockMvc.perform(
            get("/api/v1/products/{productId}", productId)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("success").value(true))
            .andExpect(jsonPath("message").value("요청에 성공했습니다."))
            .andExpect(jsonPath("data.id").value(productId))
				.andDo(
					document(
						"product-get",
						pathParameters(
							parameterWithName("productId").description("조회할 상품 ID")
						),
						responseFields(
							fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
							fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
							fieldWithPath("data.id").type(JsonFieldType.NUMBER).description("상품 ID"),
							fieldWithPath("data.name").type(JsonFieldType.STRING).description("상품 이름"),
							fieldWithPath("data.description").type(JsonFieldType.STRING).description("상품 설명"),
							fieldWithPath("data.viewCount").type(JsonFieldType.NUMBER).description("상품 조회수"),
							fieldWithPath("data.registeredAt").type(JsonFieldType.STRING).description("상품 등록 일시")
								.attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ss")),
							fieldWithPath("data.image").type(JsonFieldType.STRING).description("상품 이미지 URL"),
							fieldWithPath("data.categoryName").type(JsonFieldType.STRING).description("카테고리 이름"),
							fieldWithPath("data.soldAt").type(JsonFieldType.STRING).optional()
								.description("판매 완료 일시")
								.attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ss")),
							fieldWithPath("data.currentBid").type(JsonFieldType.NUMBER).description("현재 입찰가")
								.attributes(key("example").value("150000")),
							fieldWithPath("data.endAt").type(JsonFieldType.STRING).description("경매 종료 예정 일시")
								.attributes(key("format").value("yyyy-MM-dd'T'HH:mm:ss")),
							fieldWithPath("data.sellerName").type(JsonFieldType.STRING).description("판매자 닉네임")
						)
					)
				);
    }

    @TestConfiguration
    static class MockConfig {
        @Bean
        ProductInternalService productInternalService() {
            return Mockito.mock(ProductInternalService.class);
        }
    }
}
