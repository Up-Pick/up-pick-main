package org.oneog.uppick.domain.product.service;

import org.oneog.uppick.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductInternalService {

	private final ProductRepository productRepository;
	private final ProductQueryRepository productQueryRepository;
}
