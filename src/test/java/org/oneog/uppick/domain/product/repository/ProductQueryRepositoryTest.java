package org.oneog.uppick.domain.product.repository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.enums.Status;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.category.entity.Category;
import org.oneog.uppick.domain.category.repository.CategoryRepository;
import org.oneog.uppick.domain.member.entity.Member;
import org.oneog.uppick.domain.member.repository.MemberRepository;
import org.oneog.uppick.domain.product.dto.response.ProductInfoResponse;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.entity.SellDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class ProductQueryRepositoryTest {

	@Autowired
	private ProductQueryRepository productQueryRepository;

	@Autowired private ProductRepository productRepository;
	@Autowired private CategoryRepository categoryRepository;
	@Autowired private AuctionRepository auctionRepository;
	@Autowired private MemberRepository memberRepository;
	@Autowired private SellDetailRepository sellDetailRepository;

	private Member member;
	private Category category;
	private Product product;
	private Auction auction;
	private SellDetail sellDetail;

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

		testProductId = product.getId();
	}

	@Test
	void 상품의_상세_정보_조회_가능() {
		ProductInfoResponse result = productQueryRepository.getProductInfoById(testProductId).get();

		assertThat(result).isNotNull();
		assertThat(result.getId()).isEqualTo(testProductId);
		assertThat(result.getName()).isEqualTo(product.getName());
		assertThat(result.getDescription()).isEqualTo(product.getDescription());
		assertThat(result.getViewCount()).isEqualTo(product.getViewCount());
		assertThat(result.getRegisteredAt().toLocalDate()).isEqualTo(product.getRegisteredAt().toLocalDate());
		assertThat(result.getImage()).isEqualTo(product.getImage());
		assertThat(result.getCategoryName()).isEqualTo(String.format("%s/%s", category.getBig(), category.getSmall()));
		assertThat(result.getSoldAt()).isEqualTo(product.getSoldAt());
		assertThat(result.getCurrentBid()).isEqualTo(auction.getCurrentPrice());
		assertThat(result.getEndAt().toLocalDate()).isEqualTo(auction.getEndAt().toLocalDate());
		assertThat(result.getSellerName()).isEqualTo(member.getNickname());
	}
}
