package org.oneog.uppick.auction.domain.product.repository;

import org.oneog.uppick.auction.domain.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

	@Modifying
	@Query("UPDATE Product p SET p.viewCount = p.viewCount + :incr WHERE p.id = :id")
	void incrementViewCount(@Param("id") Long id, @Param("incr") Long incr);

}
