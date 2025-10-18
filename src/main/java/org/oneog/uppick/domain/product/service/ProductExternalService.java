package org.oneog.uppick.domain.product.service;

import org.oneog.uppick.common.exception.BusinessException;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.exception.ProductErrorCode;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductExternalService implements ProductExternalServiceApi {

	private final ProductRepository productRepository;

	@Override
	@Transactional
	public void changeProductSoldAt(Long productId) {
		Product product = getProductByIdOrElseThrow(productId);
		product.setSoldNow();
	}

	@Override
	public void deleteProduct(Long productId) {

	}

	private Product getProductByIdOrElseThrow(Long productId) {
		return productRepository.findById(productId)
			.orElseThrow(() -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND));
	}
}
