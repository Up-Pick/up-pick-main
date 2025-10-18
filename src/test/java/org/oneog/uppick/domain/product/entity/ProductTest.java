package org.oneog.uppick.domain.product.entity;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ProductTest {

	@Test
	void 판매_상품_시간이_정상적으로_변경됨() {
		Product product = Product.builder()
			.name("상품 이름")
			.description("상품 설명")
			.categoryId(1L)
			.registerId(1L)
			.build();
		LocalDateTime now = LocalDateTime.now();

		product.setSoldNow();

		assertThat(product.getSoldAt()).isNotNull();
		assertThat(product.getSoldAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(now.truncatedTo(ChronoUnit.SECONDS));
	}
}
