package org.oneog.uppick.domain.product.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.oneog.uppick.domain.auction.service.AuctionExternalServiceApi;
import org.oneog.uppick.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.mapper.ProductMapper;
import org.oneog.uppick.domain.product.repository.ProductQueryRepository;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.oneog.uppick.domain.product.repository.ProductViewHistoryRepository;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ProductInternalServiceTest {

	private final ProductMapper productMapper = new ProductMapper();

	@Mock
	ProductRepository productRepository;
	@Mock
	ProductQueryRepository productQueryRepository;
	@Mock
	ProductViewHistoryRepository productViewHistoryRepository;
	@Mock
	AuctionExternalServiceApi auctionExternalServiceApi;
	@Mock
	S3FileStorageService s3FileStorageService;

	@InjectMocks
	ProductInternalService productInternalService;

	private Product product;

	@BeforeEach
	public void init() {
		productInternalService = new ProductInternalService(
			productRepository,
			productQueryRepository,
			productViewHistoryRepository,

			productMapper,
			s3FileStorageService,
			auctionExternalServiceApi);

		product = Product.builder()
			.name("상품 이름")
			.description("상품 설명")
			.categoryId(1L)
			.registerId(1L)
			.build();

		// ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		// Validator validator = factory.getValidator();
	}

	/* ---------- Service Test ---------- */
	// @Test
	// void 상품이_이미지_없이_정상적으로_등록됨() {
	//
	// 	// given
	// 	ProductRegisterRequest request = ProductRegisterRequest
	// 		.builder()
	// 		.name("상품 이름")
	// 		.description("상품 설명")
	// 		.categoryId(1L)
	// 		.startBid(1000L)
	// 		.endAt(LocalDateTime.now().plusDays(1L))
	// 		.build();
	// 	Long registerId = 1L;
	//
	// 	// when
	// 	productInternalService.registerProduct(request, registerId);
	//
	// 	// then
	// 	ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
	// 	verify(productRepository).save(captor.capture());
	//
	// 	Product saved = captor.getValue();
	// 	assertThat(saved.getName()).isEqualTo(request.getName());
	// 	assertThat(saved.getDescription()).isEqualTo(request.getDescription());
	// 	assertThat(saved.getCategoryId()).isEqualTo(request.getCategoryId());
	// }

	@Test
	void 상품이_이미지와_정상적으로_등록됨() {

		// given
		ProductRegisterRequest request = ProductRegisterRequest
			.builder()
			.name("상품 이름")
			.description("상품 설명")
			.categoryId(1L)
			.startBid(1000L)
			.endAt(LocalDateTime.now().plusDays(1L))
			.build();
		Long registerId = 1L;

		MultipartFile image = mock(MultipartFile.class);
		when(image.isEmpty()).thenReturn(false);
		String imageUrl = "https://s3.amazonaws.com/test.jpg";
		when(s3FileStorageService.store(image)).thenReturn(imageUrl);

		// when
		productInternalService.registerProduct(request, image, registerId);

		// then
		ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
		verify(productRepository).save(captor.capture());

		Product saved = captor.getValue();
		assertThat(saved.getName()).isEqualTo(request.getName());
		assertThat(saved.getDescription()).isEqualTo(request.getDescription());
		assertThat(saved.getCategoryId()).isEqualTo(request.getCategoryId());
		assertThat(saved.getImage()).isEqualTo(imageUrl);
	}

	@Test
	void 상품을_상세_조회하면_조회수가_올라감() {

		// given
		Long prevViewCount = product.getViewCount();

		// when
		product.increaseViewCount();

		// then
		assertThat(product.getViewCount()).isEqualTo(prevViewCount + 1);
	}
}
