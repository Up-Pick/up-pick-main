package org.oneog.uppick.domain.product.mapper;

import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

	public Product registerToEntity(ProductRegisterRequest request, Long registerId, String imageUrl) {
		return Product.builder()
			.name(request.getName())
			.description(request.getDescription())
			.image(imageUrl)
			.categoryId(request.getCategoryId())
			.registerId(registerId)
			.build();
	}
}
