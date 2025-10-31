package org.oneog.uppick.auction.domain.product.mapper;

import org.oneog.uppick.auction.domain.category.dto.response.CategoryInfoResponse;
import org.oneog.uppick.auction.domain.product.document.ProductDocument;
import org.oneog.uppick.auction.domain.product.dto.projection.ProductDetailProjection;
import org.oneog.uppick.auction.domain.product.dto.projection.PurchasedProductInfoProjection;
import org.oneog.uppick.auction.domain.product.dto.projection.SoldProductInfoProjection;
import org.oneog.uppick.auction.domain.product.dto.request.ProductRegisterRequest;
import org.oneog.uppick.auction.domain.product.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductDetailResponse;
import org.oneog.uppick.auction.domain.product.dto.response.ProductSellAtResponse;
import org.oneog.uppick.auction.domain.product.dto.response.PurchasedProductInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.SearchProductInfoResponse;
import org.oneog.uppick.auction.domain.product.dto.response.SoldProductInfoResponse;
import org.oneog.uppick.auction.domain.product.entity.Product;
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

	public SearchProductInfoResponse toSearchResponse(ProductDocument document) {

		return SearchProductInfoResponse.builder()
			.id(document.getId())
			.image(document.getImage())
			.name(document.getName())
			.registeredAt(document.getRegisteredAt())
			.endAt(document.getEndAt())
			.currentBidPrice(document.getCurrentBidPrice())
			.minBidPrice(document.getMinBidPrice())
			.isSold(document.isSold())
			.build();
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

	public ProductDetailResponse combineProductDetailWithSellerAndCurrentPrice(ProductDetailProjection projection,
		String sellerName,
		Long currentPrice) {

		return ProductDetailResponse.builder()
			.id(projection.getId())
			.name(projection.getName())
			.description(projection.getDescription())
			.viewCount(projection.getViewCount())
			.registeredAt(projection.getRegisteredAt())
			.image(projection.getImage())
			.categoryName(projection.getCategoryName())
			.minPrice(projection.getMinPrice())
			.currentBid(currentPrice)
			.endAt(projection.getEndAt())
			.sellerName(sellerName)
			.build();
	}

	public ProductDocument toDocument(Product product, ProductRegisterRequest request) {

		return ProductDocument.builder()
			.id(product.getId())
			.name(product.getName())
			.image(product.getImage())
			.registeredAt(product.getRegisteredAt())
			.endAt(request.getEndAt())
			.currentBidPrice(null)
			.minBidPrice(request.getStartBid())
			.categoryId(product.getCategoryId())
			.isSold(false)
			.build();
	}

}
