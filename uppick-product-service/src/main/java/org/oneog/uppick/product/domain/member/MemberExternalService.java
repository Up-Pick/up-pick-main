package org.oneog.uppick.product.domain.member;

public interface MemberExternalService {
    void updateMemberCredit(long memberId, long l);

    void registerPurchaseDetail(Long auctionId, Long buyerId, Long productId, Long price);
}
