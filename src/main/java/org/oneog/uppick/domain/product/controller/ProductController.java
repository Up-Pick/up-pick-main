package org.oneog.uppick.domain.product.controller;

import org.oneog.uppick.domain.product.service.ProductInternalService;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductController {

	private final ProductInternalService productInternalService;
}
