package org.oneog.uppick.domain.searching.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.searching.dto.request.SearchProductRequest;
import org.oneog.uppick.domain.searching.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.domain.searching.service.SearchingInternalService;
import org.oneog.uppick.support.RestDocsBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(SearchingController.class)
public class SearchingControllerRestDocsTest extends RestDocsBase {
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SearchingInternalService searchingInternalService;

    @Test
    void searchProduct_검색시도_상품검색성공() throws Exception {
        // given
        SearchProductRequest request = SearchProductRequest.builder()
            .keyword("phone")
            .categoryId(2L)
            .onlyNotSold(true)
            .sortBy("endAt")
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

        given(searchingInternalService.searchProduct(any(SearchProductRequest.class))).willReturn(page);

        // when & then
        mockMvc.perform(
            post("/api/v1/searching/products")
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
                        fieldWithPath("keyword").type(JsonFieldType.STRING).description("검색 키워드 (공백으로 구분된 다중 키워드 가능)")
                            .optional(),
                        fieldWithPath("categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID").optional(),
                        fieldWithPath("onlyNotSold").type(JsonFieldType.BOOLEAN).description("판매중인 상품만 조회할지 여부")
                            .optional(),
                        fieldWithPath("sortBy").type(JsonFieldType.STRING).description("정렬 기준 (예: endAt, registeredAt)")
                            .optional(),
                        fieldWithPath("page").type(JsonFieldType.NUMBER).description("페이지 번호 (0부터)").optional(),
                        fieldWithPath("size").type(JsonFieldType.NUMBER).description("페이지 사이즈").optional(),
                        fieldWithPath("endAtFrom").type(JsonFieldType.STRING).description("마감일 필터 (yyyy-MM-dd 형식)")
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
