package org.oneog.uppick.domain.product.repository;

import org.oneog.uppick.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
