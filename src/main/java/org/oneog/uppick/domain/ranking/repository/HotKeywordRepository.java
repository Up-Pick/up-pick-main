package org.oneog.uppick.domain.ranking.repository;

import org.oneog.uppick.domain.ranking.entity.HotKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotKeywordRepository extends JpaRepository<HotKeyword, Long> {
}
