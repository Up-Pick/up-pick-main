package org.oneog.uppick.product.domain.product.mapper;

import org.oneog.uppick.product.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.product.domain.product.dto.projection.ProductDetailProjection;
import org.oneog.uppick.product.domain.product.dto.projection.PurchasedProductInfoProjection;
import org.oneog.uppick.product.domain.product.dto.projection.SearchProductProjection;
import org.oneog.uppick.product.domain.product.dto.projection.SoldProductInfoProjection;
import org.oneog.uppick.product.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductDetailResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellAtResponse;
import org.oneog.uppick.product.domain.product.dto.response.PurchasedProductInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.product.domain.product.dto.response.SoldProductInfoResponse;
import org.oneog.uppick.product.domain.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

	public Product registerToEntity(ProductRegisterRequest request, Long registerId, String imageUrl,
		CategoryInfoResponse categoryInfoResponse) {

		return Product.builder()
			.name(request.getName())
			.description(request.getDescription())
			.image(imageUrl)
			.categoryId(request.getCategoryId())
			.registerId(registerId)
			.bigCategory(categoryInfoResponse.getBigCategory())
			.smallCategory(categoryInfoResponse.getSmallCategory())
			.build();
	}

	public Page<SearchProductInfoResponse> toResponse(Page<SearchProductProjection> productProjections) {

		return productProjections.map(projection -> SearchProductInfoResponse.builder()
			.id(projection.getId())
			.image(projection.getImage())
			.name(projection.getName())
			.registeredAt(projection.getRegisteredAt())
			.endAt(projection.getEndAt())
			.currentBidPrice(projection.getCurrentBidPrice())
			.minBidPrice(projection.getMinBidPrice())
			.isSold(projection.isSold())
			.build());
	}

	public SoldProductInfoResponse combineSoldProductInfoWithSeller(SoldProductInfoProjection productInfo,
		ProductSellAtResponse sellerInfo) {

		return SoldProductInfoResponse.builder()
			.id(productInfo.getId())
			.name(productInfo.getName())
			.description(productInfo.getDescription())
			.image(productInfo.getImage())
			.finalPrice(productInfo.getFinalPrice())
			.soldAt(sellerInfo.getBuyAt())
			.build();
	}

	public PurchasedProductInfoResponse combinePurchasedInfoWithBuyer(PurchasedProductInfoProjection productInfo,
		ProductBuyAtResponse buyerInfo) {

		return PurchasedProductInfoResponse.builder()
			.id(productInfo.getId())
			.name(productInfo.getName())
			.image(productInfo.getImage())
			.finalPrice(productInfo.getFinalPrice())
			.buyAt(buyerInfo.getBuyAt())
			.build();
	}

	public ProductDetailResponse combineProductDetailWithSeller(ProductDetailProjection projection, String sellerName) {

		return ProductDetailResponse.builder()
			.id(projection.getId())
			.name(projection.getName())
			.description(projection.getDescription())
			.viewCount(projection.getViewCount())
			.registeredAt(projection.getRegisteredAt())
			.image(projection.getImage())
			.categoryName(projection.getCategoryName())
			.minPrice(projection.getMinPrice())
			.currentBid(projection.getCurrentBid())
			.endAt(projection.getEndAt())
			.sellerName(sellerName)
			.build();
	}

}
