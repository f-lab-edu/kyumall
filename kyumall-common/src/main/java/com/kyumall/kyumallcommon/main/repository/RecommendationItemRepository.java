package com.kyumall.kyumallcommon.main.repository;

import com.kyumall.kyumallcommon.main.entity.Recommendation;
import com.kyumall.kyumallcommon.main.entity.RecommendationItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecommendationItemRepository extends JpaRepository<RecommendationItem, Long> {

  @Query("""
  select ri 
  from RecommendationItem ri
    join fetch ri.product p
    left join fetch p.productImages pi
  where ri.recommendation in :recommendations
""")
  List<RecommendationItem> findByRecommendationIdIn(List<Recommendation> recommendations);
}
