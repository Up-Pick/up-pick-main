package org.oneog.uppick.auction.domain.category.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.domain.category.dto.response.CategoryResponse;
import org.oneog.uppick.auction.domain.category.entity.Category;
import org.oneog.uppick.auction.domain.category.repository.CategoryRepository;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class CategoryInternalServiceTest {

	@Mock
	private CategoryRepository categoryRepository;

	@InjectMocks
	private CategoryService categoryService;

	@Test
	void getAllCategories_실행시_성공() {

		// given
		Category category1 = Category.builder()
			.big("전자제품")
			.small("휴대폰")
			.build();

		Category category2 = Category.builder()
			.big("가전")
			.small("냉장고")
			.build();

		ReflectionTestUtils.setField(category1, "id", 1L);
		ReflectionTestUtils.setField(category2, "id", 2L);

		given(categoryRepository.findAll())
			.willReturn(List.of(category1, category2));

		// when
		List<CategoryResponse> responses = categoryService.getAllCategories();

		// then
		assertThat(responses).hasSize(2);
		assertThat(responses.get(0).getCategoryId()).isEqualTo(1L);
		assertThat(responses.get(0).getBigCategory()).isEqualTo("전자제품");
		assertThat(responses.get(0).getSmallCategory()).isEqualTo("휴대폰");

		assertThat(responses.get(1).getCategoryId()).isEqualTo(2L);
		assertThat(responses.get(1).getBigCategory()).isEqualTo("가전");
		assertThat(responses.get(1).getSmallCategory()).isEqualTo("냉장고");
	}

}
