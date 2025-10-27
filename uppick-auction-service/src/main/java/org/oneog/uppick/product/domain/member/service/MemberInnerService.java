package org.oneog.uppick.product.domain.member.service;

import java.util.List;

import org.oneog.uppick.product.domain.member.dto.request.RegisterPurchaseDetailRequest;
import org.oneog.uppick.product.domain.member.dto.request.RegisterSellDetailRequest;
import org.oneog.uppick.product.domain.product.dto.response.ProductBuyAtResponse;
import org.oneog.uppick.product.domain.product.dto.response.ProductSellAtResponse;

public interface MemberInnerService {

	long getMemberCredit(long memberId);

	String getMemberNickname(long memberId);

	List<ProductBuyAtResponse> getProductBuyAt(List<Long> productIds);

	List<ProductSellAtResponse> getProductSellAt(List<Long> productIds);

	void registerPurchaseDetail(RegisterPurchaseDetailRequest request);

	void registerSellDetail(RegisterSellDetailRequest request);

	void updateMemberCredit(long memberId, long credit);

}
