INSERT INTO member (id, email, nickname, password, credit, registered_at) VALUES
	(1, 'alice@uppick.com', '앨리스원더', '$2a$10$Aqj6LEh1iEMbJg4oVg8zJeXnR9y1pEprbDqraYt6kAd8b5QcO.qBu', 520000, '2025-10-04 09:15:00'),
	(2, 'brian@uppick.com', '브라이언딜즈', '$2a$10$BhvmFxQwZXsEwz2pr0Ah/.AwNSMUTQpXjbD8v7uUoA6gYbUn0bHnq', 275000, '2025-09-06 11:42:00'),
	(3, 'chloe@uppick.com', '클로에입찰', '$2a$10$5WReD2k1FqAzmEheB2oT2OC2QbMiHsdL9X0wZe0jB1s/5Nz6AgU7S', 840000, '2025-08-08 19:05:00'),
	(4, 'diego@uppick.com', '디에고콜렉터', '$2a$10$VS13c8YjYvZczYI6QPYhV.89O8J8QdgnP8wLhLX.NJFdqA0zGDG9e', 610000, '2025-07-09 08:22:00'),
	(5, 'emma@uppick.com', '엠마파인즈', '$2a$10$gv6Y8R/pC/wz6mNVB5bgvOaIvUKbZlSLQh.DezX7oY6V5k6m9cb1a', 455000, '2025-09-11 14:57:00');

INSERT INTO purchase_detail (id, auction_id, product_id, buyer_id, purchase_price, purchase_at) VALUES
	(1, 2, 2, 4, 1800000, '2025-09-15 20:05:00'),
	(2, 4, 4, 5, 350000, '2025-08-15 19:10:00');

INSERT INTO sell_detail (id, final_price, sell_at, auction_id, product_id, seller_id) VALUES
	(1, 1800000, '2025-09-15 20:06:00', 2, 2, 2),
	(2, 350000, '2025-08-15 19:11:00', 4, 4, 4);

INSERT INTO notification (id, member_id, type, title, message, notified_at, is_read) VALUES
	(1, 3, 'BID', '입찰이 최고가를 경신했어요', '회원님의 입찰이 경매 #1에서 최고가가 되었습니다.', '2025-10-23 09:20:00', FALSE),
	(2, 5, 'BID', '입찰 알림', '경매 #1에 새로운 입찰이 등록되었습니다. 확인해 보세요.', '2025-10-22 14:25:00', TRUE),
	(3, 4, 'TRADE', '낙찰을 축하드립니다', '경매 #2에서 맥북 에어 M3를 낙찰 받았습니다.', '2025-09-15 20:06:00', FALSE),
	(4, 2, 'TRADE', '상품이 판매되었어요', '경매 #2 상품이 최종 낙찰되었습니다.', '2025-09-15 20:07:00', TRUE),
	(5, 5, 'TRADE', '판매 대금이 확정되었습니다', '경매 #4 상품의 판매가 완료되었습니다.', '2025-08-15 19:15:00', FALSE);

INSERT INTO hot_keyword (id, keyword, rank_no) VALUES
	(1, '맥북', 1),
	(2, '리자몽', 2),
	(3, '나이키 스니커즈', 3),
	(4, '태블릿 펜', 4),
	(5, '커피머신', 5);

INSERT INTO search_history (id, keyword, searched_at) VALUES
	(1, '맥북 m3', '2025-10-24 09:15:00'),
	(2, '갤럭시 탭 번들', '2025-10-24 10:02:00'),
	(3, '포켓몬 홀로카드', '2025-10-23 21:18:00'),
	(4, '나이키 에어맥스 270', '2025-10-22 17:44:00'),
	(5, '드롱기 매그니피카', '2025-10-21 08:27:00'),
	(6, '게이밍 노트북', '2025-10-20 22:11:00'),
	(7, '한정판 스니커즈', '2025-10-20 07:35:00'),
	(8, '커피 그라인더', '2025-10-19 12:53:00'),
	(9, '희귀 트레이딩 카드', '2025-10-18 19:05:00'),
	(10, '무선 스타일러스', '2025-10-17 09:42:00');