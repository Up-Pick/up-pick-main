package org.oneog.uppick.domain.ranking.repository;

import org.oneog.uppick.domain.ranking.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}
