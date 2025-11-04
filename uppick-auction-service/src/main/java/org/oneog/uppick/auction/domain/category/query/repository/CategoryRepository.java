package org.oneog.uppick.auction.domain.category.query.repository;

import org.oneog.uppick.auction.domain.category.query.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
