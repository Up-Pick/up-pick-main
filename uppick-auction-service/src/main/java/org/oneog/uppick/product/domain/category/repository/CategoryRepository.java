package org.oneog.uppick.product.domain.category.repository;

import org.oneog.uppick.product.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
