package org.oneog.uppick.auction.domain.category.repository;

import org.oneog.uppick.auction.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

}
