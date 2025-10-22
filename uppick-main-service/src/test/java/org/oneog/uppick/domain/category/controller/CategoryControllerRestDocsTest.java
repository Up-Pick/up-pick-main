package org.oneog.uppick.domain.category.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.category.dto.response.CategoryResponse;
import org.oneog.uppick.domain.category.service.CategoryInternalService;
import org.oneog.uppick.support.restdocs.RestDocsBase;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(CategoryController.class)
class CategoryControllerRestDocsTest extends RestDocsBase {

	@MockitoBean
	private CategoryInternalService categoryInternalService;

	@Test
	@DisplayName("전체 카테고리 조회 API를 Rest Docs로 문서화한다")
	void GetAllCategories_정상적인조회_조회성공() throws Exception {
		// given
		// Mocking 데이터 생성: CategoryResponse 리스트
		List<CategoryResponse> mockCategories = List.of(
			new CategoryResponse(1L, "전자제품", "노트북"),
			new CategoryResponse(2L, "전자제품", "태블릿"),
			new CategoryResponse(3L, "패션", "여성 의류"),
			new CategoryResponse(4L, "패션", "남성 의류"));

		// Mocking: Service 호출 시 위 리스트 반환 설정
		given(categoryInternalService.getAllcategories()).willReturn(mockCategories);

		// when & then
		mockMvc.perform(
			get("/api/v1/categories")
				.accept(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andExpect(jsonPath("success").value(true))
			.andExpect(jsonPath("data.size()").value(mockCategories.size()))
			.andDo(
				document(
					"category-get-all", // 스니펫 디렉토리 이름
					responseFields( // GlobalApiResponse<List<CategoryResponse>>에 대한 응답 필드
						fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("요청 성공 여부"),
						fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지"),
						fieldWithPath("data").type(JsonFieldType.ARRAY).description("전체 카테고리 목록"),
						fieldWithPath("data[].categoryId").type(JsonFieldType.NUMBER).description("카테고리 ID"),
						fieldWithPath("data[].bigCategory").type(JsonFieldType.STRING).description("대분류 카테고리 명"),
						fieldWithPath("data[].smallCategory").type(JsonFieldType.STRING).description("소분류 카테고리 명"))));
	}
}
