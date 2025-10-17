package org.oneog.uppick.domain.searching.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.oneog.uppick.domain.auction.entity.Auction;
import org.oneog.uppick.domain.auction.enums.Status;
import org.oneog.uppick.domain.auction.repository.AuctionRepository;
import org.oneog.uppick.domain.product.entity.Product;
import org.oneog.uppick.domain.product.repository.ProductRepository;
import org.oneog.uppick.domain.searching.dto.projection.SearchProductProjection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class SearchingQueryRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private SearchingQueryRepository searchingQueryRepository;

    private Product p1; // category 1
    private Product p2; // category 2

    @BeforeEach
    void setUp() {
        auctionRepository.deleteAll();
        productRepository.deleteAll();

        p1 = Product.builder()
            .name("상품1")
            .description("설명1")
            .image("img1.png")
            .categoryId(1L)
            .registerId(100L)
            .build();

        p2 = Product.builder()
            .name("상품2")
            .description("설명2")
            .image("img2.png")
            .categoryId(2L)
            .registerId(101L)
            .build();

        productRepository.saveAll(List.of(p1, p2));

        // p1: 진행중 경매
        Auction a1 = Auction.builder()
            .productId(p1.getId())
            .currentPrice(500L)
            .minPrice(100L)
            .status(Status.IN_PROGRESS)
            .endAt(LocalDateTime.now().plusDays(1))
            .build();

        // p2: 종료된 경매
        Auction a2 = Auction.builder()
            .productId(p2.getId())
            .currentPrice(1000L)
            .minPrice(200L)
            .status(Status.FINISHED)
            .endAt(LocalDateTime.now().minusDays(1))
            .build();

        auctionRepository.saveAll(List.of(a1, a2));
    }

    @Test
    void findProductsWithFilters_기본조회_카테고리1만반환() {
        // categoryId가 null일 경우 레포지토리 내부에서 기본값으로 1을 사용하도록 되어 있음
        Page<SearchProductProjection> page = searchingQueryRepository.findProductsWithFilters(PageRequest.of(0, 10),
            1L, null, false, null, null);

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        List<SearchProductProjection> content = page.getContent();
        // 기본 카테고리(1)에 속한 상품1이 포함되어야 함
        assertThat(content).extracting("name").contains("상품1");
    }

    @Test
    void findProductsWithFilters_카테고리필터_해당카테고리만반환() {
        Page<SearchProductProjection> page = searchingQueryRepository.findProductsWithFilters(PageRequest.of(0, 10), 2L,
            null, false, null, null);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().get(0).getName()).isEqualTo("상품2");
    }

    @Test
    void findProductsWithFilters_마감시작일필터_해당이후만반환() {
        LocalDateTime from = LocalDateTime.now().plusHours(12);
        Page<SearchProductProjection> page = searchingQueryRepository.findProductsWithFilters(PageRequest.of(0, 10),
            1L, from, false, null, null);

        // p1의 endAt은 now+1 day -> 포함, p2는 과거 -> 제외
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getContent()).allMatch(p -> p.getEndAt().isAfter(from) || p.getEndAt().isEqual(from));
    }

    @Test
    void findProductsWithFilters_미판매필터_판매완료제외() {
        Page<SearchProductProjection> page = searchingQueryRepository.findProductsWithFilters(PageRequest.of(0, 10),
            1L, null, true, null, null);

        // p2는 FINISHED이므로 제외되어야 함
        assertThat(page.getContent()).noneMatch(p -> Boolean.TRUE.equals(p.isSold()));
    }

    @Test
    void findProductsWithFilters_정렬_endAtDesc_정렬적용() {
        Page<SearchProductProjection> page = searchingQueryRepository.findProductsWithFilters(PageRequest.of(0, 10),
            1L, null, false, "endAtDesc", null);

        List<SearchProductProjection> content = page.getContent();
        if (content.size() >= 2) {
            // 내림차순이면 첫번째 endAt이 두번째보다 늦거나 같아야 함
            assertThat(content.get(0).getEndAt()).isAfterOrEqualTo(content.get(1).getEndAt());
        }
    }

    @Test
    void findProductsWithFilters_keyword검색_상품제목기준으로필터링() {
        // keyword로 '상품1'을 검색하면 '상품1'만 포함되어야 함
        Page<SearchProductProjection> page = searchingQueryRepository.findProductsWithFilters(PageRequest.of(0, 10),
            1L, null, false, null, "상품1");

        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        List<SearchProductProjection> content = page.getContent();
        // 결과에 '상품1'이 포함되고 '상품2'는 포함되지 않아야 함
        assertThat(content).extracting(SearchProductProjection::getName).contains("상품1").doesNotContain("상품2");
    }
}
