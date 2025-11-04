package org.oneog.uppick.domain.ranking.command.repository;

import java.util.List;

import org.oneog.uppick.domain.ranking.command.entity.HotKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotKeywordRepository extends JpaRepository<HotKeyword, Long> {

	List<HotKeyword> findAllByOrderByRankNoAsc();
}
