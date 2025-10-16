package org.oneog.uppick.domain.product.controller;

import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.service.ProductInternalService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductInternalService productInternalService;

	@PostMapping
	public GlobalApiResponse<Object> registerProduct(
		@Valid @RequestBody ProductRegisterRequest request,
		@AuthenticationPrincipal AuthMember authMember) {

		productInternalService.registerProduct(request, authMember.getMemberId());
		return GlobalApiResponse.ok(null);
	}
}
