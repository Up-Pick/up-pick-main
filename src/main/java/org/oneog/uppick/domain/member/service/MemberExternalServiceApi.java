package org.oneog.uppick.domain.member.service;

public interface MemberExternalServiceApi {

	/**
	 * 구매 내역 등록
	 *
	 * @param auctionId 경매 ID
	 * @param buyerId 구매자 회원 ID
	 * @param productId 상품 ID
	 * @param price 거래 금액
	 */
	default void registerPurchaseDetail(Long auctionId, Long buyerId, Long productId, Long price) {

	}

	;

	/**
	 *  판매 내역 등록
	 *
	 * @param auctionId 경매 ID
	 * @param sellerId 판매자 회원 ID
	 * @param productId 상품 ID
	 * @param price 거래 금액
	 */
	default void registerSellDetail(Long auctionId, Long sellerId, Long productId, Long price) {

	}

	;
	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	void createUser(String email, String nickname, String encodedPassword);
}
