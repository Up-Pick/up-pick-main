package org.oneog.uppick.auction.domain.product.command.service;

import org.oneog.uppick.auction.domain.auction.command.service.AuctionInnerService;
import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.category.query.service.CategoryInnerService;
import org.oneog.uppick.auction.domain.product.command.entity.Product;
import org.oneog.uppick.auction.domain.product.command.model.dto.request.ProductRegisterRequest;
import org.oneog.uppick.auction.domain.product.command.repository.ProductDocumentRepository;
import org.oneog.uppick.auction.domain.product.command.repository.ProductRepository;
import org.oneog.uppick.auction.domain.product.common.S3FileManager;
import org.oneog.uppick.auction.domain.product.common.document.ProductDocument;
import org.oneog.uppick.auction.domain.product.common.exception.ProductErrorCode;
import org.oneog.uppick.auction.domain.product.common.mapper.ProductMapper;
import org.oneog.uppick.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCommandService {

	// ***** Product Domain ***** //
	private final ProductRepository productRepository;
	private final ProductDocumentRepository productDocumentRepository;
	private final ProductMapper productMapper;

	// ****** S3 ***** //
	private final S3FileManager s3FileManager;

	// ****** External Domain API ***** //
	private final AuctionInnerService auctionInnerService;
	private final CategoryInnerService categoryInnerService;

	// ***** Internal Service Method ***** //
	@Transactional
	public void registerProduct(ProductRegisterRequest request, MultipartFile image, Long registerId) {

		// 1. 이미지 검증
		if (image == null || image.isEmpty()) {
			throw new BusinessException(ProductErrorCode.EMPTY_FILE);
		}

		// 2. S3에 이미지 업로드
		String imageUrl = s3FileManager.store(image);

		// 3. Product 엔티티 생성 (imageUrl 포함)
		CategoryInfoResponse category = categoryInnerService.getCategoriesByCategoryId(request.getCategoryId());
		Product product = productMapper.registerToEntity(request, registerId, imageUrl, category);

		// 상품 및 경매 등록
		Product savedProduct = productRepository.save(product);
		auctionInnerService.registerAuction(savedProduct.getId(), registerId, request.getStartBid(),
			savedProduct.getRegisteredAt(), request.getEndAt());

		// Elasticsearch 저장
		ProductDocument document = productMapper.toDocument(savedProduct, request);
		productDocumentRepository.save(document);
	}

}
