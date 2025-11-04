package org.oneog.uppick.auction.domain.product.command.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.auction.domain.auction.command.service.AuctionInnerService;
import org.oneog.uppick.auction.domain.category.query.model.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.category.query.service.CategoryInnerService;
import org.oneog.uppick.auction.domain.product.command.entity.Product;
import org.oneog.uppick.auction.domain.product.command.model.dto.request.ProductRegisterRequest;
import org.oneog.uppick.auction.domain.product.command.repository.ProductDocumentRepository;
import org.oneog.uppick.auction.domain.product.command.repository.ProductRepository;
import org.oneog.uppick.auction.domain.product.common.S3FileManager;
import org.oneog.uppick.auction.domain.product.common.document.ProductDocument;
import org.oneog.uppick.auction.domain.product.common.mapper.ProductMapper;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProductCommandServiceTest {

	private final ProductMapper productMapper = new ProductMapper();

	@Mock
	ProductDocumentRepository productDocumentRepository;
	@Mock
	ProductRepository productRepository;
	@Mock
	S3FileManager s3FileManager;
	@Mock
	AuctionInnerService auctionInnerService;
	@Mock
	CategoryInnerService categoryInnerService;

	ProductCommandService productCommandService;

	@BeforeEach
	public void init() {

		productCommandService = new ProductCommandService(
			productRepository,
			productDocumentRepository,
			productMapper,
			s3FileManager,
			auctionInnerService,
			categoryInnerService);
	}

	@Test
	void 상품이_이미지와_함께_정상적으로_등록됨() {

		// given
		ProductRegisterRequest request = ProductRegisterRequest
			.builder()
			.name("상품 이름")
			.description("상품 설명")
			.categoryId(1L)
			.startBid(1000L)
			.endAt(LocalDateTime.now().plusDays(1L))
			.build();
		MultipartFile image = mock(MultipartFile.class);
		Long registerId = 1L;
		String imageUrl = "image.jpg";

		CategoryInfoResponse categoryInfoResponse = new CategoryInfoResponse("대분류", "소분류");

		Product saved = productMapper.registerToEntity(request, registerId, imageUrl, categoryInfoResponse);
		ReflectionTestUtils.setField(saved, "id", 1L);

		given(s3FileManager.store(image)).willReturn(imageUrl);
		given(categoryInnerService.getCategoriesByCategoryId(request.getCategoryId())).willReturn(categoryInfoResponse);
		given(productRepository.save(any(Product.class))).willReturn(saved);
		given(productDocumentRepository.save(any(ProductDocument.class))).willReturn(new ProductDocument());

		// when
		productCommandService.registerProduct(request, image, registerId);

		// then
		verify(s3FileManager).store(image);
		verify(productRepository).save(any(Product.class));

		verify(auctionInnerService).registerAuction(saved.getId(), registerId, request.getStartBid(),
			saved.getRegisteredAt(), request.getEndAt());

		assertThat(saved.getName()).isEqualTo(request.getName());
		assertThat(saved.getDescription()).isEqualTo(request.getDescription());
		assertThat(saved.getCategoryId()).isEqualTo(request.getCategoryId());
		assertThat(saved.getImage()).isEqualTo(imageUrl);
		assertThat(saved.getCategoryId()).isEqualTo(request.getCategoryId());
		assertThat(saved.getRegisterId()).isEqualTo(registerId);
		assertThat(saved.getBigCategory()).isEqualTo(categoryInfoResponse.getBigCategory());
		assertThat(saved.getSmallCategory()).isEqualTo(categoryInfoResponse.getSmallCategory());
	}

}
