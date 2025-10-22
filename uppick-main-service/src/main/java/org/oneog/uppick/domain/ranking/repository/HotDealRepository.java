package org.oneog.uppick.domain.ranking.repository;

import java.util.List;

import org.oneog.uppick.domain.ranking.entity.HotDeal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HotDealRepository extends JpaRepository<HotDeal, Long> {

	List<HotDeal> findAllByOrderByRankNoAsc();

}
