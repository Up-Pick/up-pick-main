package org.oneog.uppick.auction.domain.member.service;

import java.util.List;

import org.oneog.uppick.auction.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.auction.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.auction.domain.product.query.model.dto.response.ProductSellAtResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberInnerService {

	long getMemberCredit(long memberId);

	String getMemberNickname(long memberId);

	List<ProductBuyAtResponse> getProductBuyAt(List<Long> productIds);

	List<ProductSellAtResponse> getProductSellAt(List<Long> productIds);

	Page<ProductSellAtResponse> getProductSellAtByMemberId(Long memberId,
		Pageable pageable);

	void registerPurchaseDetail(RegisterPurchaseDetailRequest request);

	void registerSellDetail(RegisterSellDetailRequest request);

	void updateMemberCredit(long memberId, long credit);

}
