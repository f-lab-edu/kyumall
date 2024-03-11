package com.kyumall.kyumallcommon.main.repository;

import com.kyumall.kyumallcommon.main.entity.Recommendation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

  @Query("select distinct r "
      + "from Recommendation r "
      + "join fetch r.recommendationItems ri "
      + "join fetch ri.product "
      + "where r.inUse = true "
      + "order by r.sortOrder")
  List<Recommendation> findAllInUse();
}
