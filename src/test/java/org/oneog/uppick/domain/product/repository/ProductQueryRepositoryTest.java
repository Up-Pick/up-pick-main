package org.oneog.uppick.domain.product.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.enums.Status;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.category.entity.Category;
import org.oneog.uppick.domain.category.repository.CategoryRepository;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.entity.PurchaseDetail;
import org.oneog.uppick.domain.member.entity.SellDetail;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.oneog.uppick.domain.member.repository.PurchaseDetailRepository;
import org.oneog.uppick.domain.member.repository.SellDetailRepository;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductPurchasedInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSimpleInfoResponse;
import org.oneog.uppick.domain.product.dto.response.ProductSoldInfoResponse;
import org.oneog.uppick.domain.product.entity.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ProductQueryRepositoryTest {

	@Autowired
	private ProductQueryRepository productQueryRepository;

	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private CategoryRepository categoryRepository;
	@Autowired
	private AuctionRepository auctionRepository;
	@Autowired
	private MemberRepository memberRepository;
	@Autowired
	private SellDetailRepository sellDetailRepository;
	@Autowired
	private PurchaseDetailRepository purchaseDetailRepository;

	private Member member;
	private Category category;
	private Product product;
	private Auction auction;
	private SellDetail sellDetail;
	private PurchaseDetail purchaseDetail;

	private Long testProductId;

	@BeforeEach
	void init() {
		member = Member.builder()
			.name("이름")
			.email("이메일")
			.nickname("닉네임")
			.password("패스워드")
			.credit(100_000L)
			.build();
		Member savedMember = memberRepository.save(member);

		category = Category.builder()
			.big("컴퓨터")
			.small("키보드")
			.build();
		Category savedCategory = categoryRepository.save(category);

		product = Product.builder()
			.name("상품 이름")
			.description("상품 설명")
			.image("상품 이미지 경로")
			.categoryId(savedCategory.getId())
			.registerId(savedMember.getId())
			.build();
		Product savedProduct = productRepository.save(product);

		auction = Auction.builder()
			.productId(savedProduct.getId())
			.currentPrice(30_000L)
			.minPrice(10_000L)
			.status(Status.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(3L))
			.build();
		Auction savedAuction = auctionRepository.save(auction);

		sellDetail = SellDetail.builder()
			.finalPrice(3_000L)
			.auctionId(savedAuction.getId())
			.productId(savedProduct.getId())
			.sellerId(savedMember.getId())
			.build();
		sellDetailRepository.save(sellDetail);

		purchaseDetail = PurchaseDetail.builder()
			.auctionId(savedAuction.getId())
			.productId(savedProduct.getId())
			.buyerId(savedMember.getId())
			.purchasePrice(45_000L)
			.build();
		purchaseDetailRepository.save(purchaseDetail);

		testProductId = product.getId();
	}

	@Test
	void 상품의_상세_정보_조회_가능() {
		ProductInfoResponse result = productQueryRepository.getProductInfoById(testProductId).orElseThrow();

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(testProductId);
		assertThat(result.getName()).isEqualTo(product.getName());
		assertThat(result.getDescription()).isEqualTo(product.getDescription());
		assertThat(result.getViewCount()).isEqualTo(product.getViewCount());
		assertThat(result.getRegisteredAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(
			product.getRegisteredAt().truncatedTo(ChronoUnit.SECONDS));
		assertThat(result.getImage()).isEqualTo(product.getImage());
		assertThat(result.getCategoryName()).isEqualTo(String.format("%s/%s", category.getBig(), category.getSmall()));
		assertThat(result.getSoldAt()).isEqualTo(product.getSoldAt());
		assertThat(result.getMinPrice()).isEqualTo(auction.getMinPrice());
		assertThat(result.getCurrentBid()).isEqualTo(auction.getCurrentPrice());
		assertThat(result.getEndAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(
			auction.getEndAt().truncatedTo(ChronoUnit.SECONDS));
		assertThat(result.getSellerName()).isEqualTo(member.getNickname());
	}

	@Test
	void 판매_완료된_상품의_정보_조회_가능() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<ProductSoldInfoResponse> results = productQueryRepository.getProductSoldInfoByMemberId(member.getId(),
			pageable);

		assertThat(results).isNotNull();

		ProductSoldInfoResponse result = results.getContent().getFirst();
		assertThat(result.getId()).isEqualTo(product.getId());
		assertThat(result.getName()).isEqualTo(product.getName());
		assertThat(result.getDescription()).isEqualTo(product.getDescription());
		assertThat(result.getImage()).isEqualTo(product.getImage());
		assertThat(result.getFinalPrice()).isEqualTo(sellDetail.getFinalPrice());
		assertThat(result.getSoldAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(
			sellDetail.getSellAt().truncatedTo(ChronoUnit.SECONDS));
	}

	@Test
	void 입찰중인_상품의_간단한_정보_조회_가능() {
		ProductSimpleInfoResponse result = productQueryRepository.getProductSimpleInfoById(product.getId())
			.orElseThrow();

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(product.getName());
		assertThat(result.getImage()).isEqualTo(product.getImage());
		assertThat(result.getMinBidPrice()).isEqualTo(auction.getMinPrice());
		assertThat(result.getCurrentBidPrice()).isEqualTo(auction.getCurrentPrice());
	}

	@Test
	void 입찰한_사람이_없는_상품의_간단한_정보_조회_가능() {
		Product newProduct = Product.builder()
			.name("새 상품 이름")
			.description("새 상품 설명")
			.image("새 상품 이미지 경로")
			.categoryId(category.getId())
			.registerId(member.getId())
			.build();
		Product savedProduct = productRepository.save(newProduct);

		Auction newAuction = Auction.builder()
			.productId(savedProduct.getId())
			// .currentPrice(30_000L) -> currentPrice가 Null인 경우
			.minPrice(10_000L)
			.status(Status.IN_PROGRESS)
			.endAt(LocalDateTime.now().plusDays(3L))
			.build();
		auctionRepository.save(newAuction);

		ProductSimpleInfoResponse result = productQueryRepository.getProductSimpleInfoById(newProduct.getId())
			.orElseThrow();

		assertThat(result).isNotNull();
		assertThat(result.getName()).isEqualTo(newProduct.getName());
		assertThat(result.getImage()).isEqualTo(newProduct.getImage());
		assertThat(result.getMinBidPrice()).isEqualTo(newAuction.getMinPrice());
		assertThat(result.getCurrentBidPrice()).isNull();
	}

	@Test
	void 구매_완료한_상품_목록_조회_가능() {
		Pageable pageable = PageRequest.of(0, 20);
		Page<ProductPurchasedInfoResponse> results = productQueryRepository.getPurchasedProductInfoByMemberId(
			member.getId(), pageable);

		assertThat(results).isNotNull();

		ProductPurchasedInfoResponse result = results.getContent().getFirst();
		assertThat(result.getId()).isEqualTo(product.getId());
		assertThat(result.getName()).isEqualTo(product.getName());
		assertThat(result.getImage()).isEqualTo(product.getImage());
		assertThat(result.getFinalPrice()).isEqualTo(purchaseDetail.getPurchasePrice());
		assertThat(result.getBuyAt().truncatedTo(ChronoUnit.SECONDS)).isEqualTo(
			purchaseDetail.getPurchaseAt().truncatedTo(ChronoUnit.SECONDS));
	}
}
