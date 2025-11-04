package org.oneog.uppick.auction.domain.product.command.controller;

import org.oneog.uppick.auction.domain.product.command.model.dto.request.ProductRegisterRequest;
import org.oneog.uppick.auction.domain.product.command.service.ProductCommandService;
import org.oneog.uppick.common.dto.AuthMember;
import org.oneog.uppick.common.dto.GlobalApiResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductCommandController {

	private final ProductCommandService productCommandService;

	// 판매 상품 등록
	@PreAuthorize("isAuthenticated()")
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public GlobalApiResponse<Void> registerProduct(@Valid @RequestPart("product") ProductRegisterRequest request,
		@RequestPart("image") MultipartFile image, @AuthenticationPrincipal AuthMember authMember) {

		productCommandService.registerProduct(request, image, authMember.getMemberId());
		return GlobalApiResponse.ok(null);
	}

}
