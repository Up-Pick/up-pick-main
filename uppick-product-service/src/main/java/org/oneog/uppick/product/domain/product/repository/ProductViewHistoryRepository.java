package org.oneog.uppick.product.domain.product.repository;

import org.oneog.uppick.product.domain.product.entity.ProductViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductViewHistoryRepository extends JpaRepository<ProductViewHistory, Long> {
}
