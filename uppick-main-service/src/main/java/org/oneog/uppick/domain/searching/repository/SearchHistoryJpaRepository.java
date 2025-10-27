package org.oneog.uppick.domain.searching.repository;

import org.oneog.uppick.domain.searching.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistory, Long> {

}
