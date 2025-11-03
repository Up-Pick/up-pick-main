INSERT INTO product (id, name, description, view_count, registered_at, image, category_id, register_id, big_category,
                     small_category)
VALUES (1, '갤럭시 탭 S9', 'S-Pen 포함 12.4인치 AMOLED 디스플레이, 2개월 가량 소량 사용.', 125, '2025-10-10 12:30:00',
        'https://cdn.uppick/images/products/galaxy-tab-s9.jpg', 1, 1, '전자제품', '태블릿'),
       (2, '맥북 에어 M3', '13인치, 16GB RAM, 512GB SSD. 정품 박스 및 충전기 포함.', 214, '2025-08-30 18:45:00',
        'https://cdn.uppick/images/products/macbook-air-m3.jpg', 2, 2, '전자제품', '노트북'),
       (3, '나이키 에어맥스 270', '미사용 새제품, 사이즈 260mm, 특별 컬러웨이.', 89, '2025-08-20 09:20:00',
        'https://cdn.uppick/images/products/nike-air-max-270.jpg', 3, 3, '패션', '스니커즈'),
       (4, '포켓몬 리자몽 홀로카드', '1세대 홀로그램 리자몽, 민트 상태(최상).', 177, '2025-07-25 16:05:00',
        'https://cdn.uppick/images/products/charizard-holo.jpg', 4, 4, '수집품', '트레이딩 카드'),
       (5, '드롱기 매그니피카 S', '자동 커피머신, 필터 최근 교체됨.', 142, '2025-10-01 21:40:00',
        'https://cdn.uppick/images/products/delonghi-magnifica.jpg', 5, 2, '생활가전', '커피머신');

INSERT INTO auction (id, product_id, last_bidder_id, register_id, current_price, min_price, status, start_at, end_at)
VALUES (1, 1, 3, 1, 620000, 500000, 'IN_PROGRESS', '2025-10-20 09:00:00', '2025-11-05 21:00:00'),
       (2, 2, 4, 2, 1800000, 1400000, 'FINISHED', '2025-09-10 12:00:00', '2025-09-15 20:00:00'),
       (3, 3, NULL, 3, NULL, 150000, 'EXPIRED', '2025-09-01 08:30:00', '2025-09-07 22:00:00'),
       (4, 4, 5, 4, 350000, 200000, 'FINISHED', '2025-08-10 10:00:00', '2025-08-15 19:00:00'),
       (5, 5, 1, 2, 280000, 250000, 'IN_PROGRESS', '2025-10-25 07:30:00', '2025-10-30 23:00:00');

INSERT INTO bidding_detail (id, auction_id, bidder_id, bid_price, bid_at)
VALUES (1, 1, 2, 540000, '2025-10-21 10:05:00'),
       (2, 1, 5, 575000, '2025-10-22 14:22:00'),
       (3, 1, 3, 620000, '2025-10-23 09:18:00'),
       (4, 2, 3, 1600000, '2025-09-12 11:45:00'),
       (5, 2, 4, 1800000, '2025-09-15 19:52:00'),
       (6, 4, 2, 260000, '2025-08-12 15:33:00'),
       (7, 4, 5, 350000, '2025-08-14 18:07:00'),
       (8, 5, 3, 260000, '2025-10-26 08:15:00'),
       (9, 5, 1, 280000, '2025-10-26 13:40:00');

INSERT INTO category (id, big, small, created_at)
VALUES (1, '전자제품', '태블릿', '2025-10-01 00:10:00'),
       (2, '전자제품', '노트북', '2025-10-01 00:15:00'),
       (3, '패션', '스니커즈', '2025-10-01 00:20:00'),
       (4, '수집품', '트레이딩 카드', '2025-10-01 00:25:00'),
       (5, '생활가전', '커피머신', '2025-10-01 00:30:00');