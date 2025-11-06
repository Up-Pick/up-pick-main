```
DELETE /product

PUT /product
{
  "settings": {
    "index": {
      "number_of_shards": 1,
      "number_of_replicas": 0
    }
  },
  "mappings": {
    "properties": {
      "name": { "type": "text", "analyzer": "nori" },
      "image": { "type": "text" },
      "registered_at": { "type": "date", "format": "yyyy-MM-dd'T'HH:mm" },
      "end_at": { "type": "date", "format": "yyyy-MM-dd'T'HH:mm" },
      "current_bid_price": { "type": "long" },
      "min_bid_price": { "type": "long" },
      "is_sold": { "type": "boolean" },
      "category_id": { "type": "long" }
    }
  }
}

POST /_bulk
{ "index": { "_index": "product", "_id": "1" } }
{ "id": 1, "name": "갤럭시 탭 S9", "image": "https://cdn.uppick/images/products/galaxy-tab-s9.jpg", "registered_at": "2025-10-10T12:30", "end_at": "2025-11-05T21:00", "current_bid_price": 620000, "min_bid_price": 500000, "is_sold": false, "category_id": 1 }
{ "index": { "_index": "product", "_id": "2" } }
{ "id": 2, "name": "맥북 에어 M3", "image": "https://cdn.uppick/images/products/macbook-air-m3.jpg", "registered_at": "2025-08-30T18:45", "end_at": "2025-09-15T20:00", "current_bid_price": 1800000, "min_bid_price": 1400000, "is_sold": true, "category_id": 2 }
{ "index": { "_index": "product", "_id": "3" } }
{ "id": 3, "name": "나이키 에어맥스 270", "image": "https://cdn.uppick/images/products/nike-air-max-270.jpg", "registered_at": "2025-08-20T09:20", "end_at": "2025-09-07T22:00", "current_bid_price": null, "min_bid_price": 150000, "is_sold": false, "category_id": 3 }
{ "index": { "_index": "product", "_id": "4" } }
{ "id": 4, "name": "포켓몬 리자몽 홀로카드", "image": "https://cdn.uppick/images/products/charizard-holo.jpg", "registered_at": "2025-07-25T16:05", "end_at": "2025-08-15T19:00", "current_bid_price": 350000, "min_bid_price": 200000, "is_sold": true, "category_id": 4 }
{ "index": { "_index": "product", "_id": "5" } }
{ "id": 5, "name": "드롱기 매그니피카 S", "image": "https://cdn.uppick/images/products/delonghi-magnifica.jpg", "registered_at": "2025-10-01T21:40", "end_at": "2025-10-30T23:00", "current_bid_price": 280000, "min_bid_price": 250000, "is_sold": false, "category_id": 5 }
```

### 실행 팁
1. Kibana Dev Tools 콘솔에 이 파일 전체를 붙여넣고 실행하세요.
2. 날짜 형식은 ProductDocument의 패턴(yyyy-MM-dd'T'HH:mm)에 맞춰 구성했습니다.
3. 필요 시 매핑(샤드/레플리카, analyzer 등)을 프로젝트 설정에 맞게 조정하세요.
