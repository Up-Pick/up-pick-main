package org.oneog.uppick.domain.searching.command.repository;

import org.oneog.uppick.domain.searching.command.entity.SearchHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchHistoryJpaRepository extends JpaRepository<SearchHistory, Long> {

}
