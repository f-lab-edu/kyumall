package com.kyumall.kyumallcommon.main.repository;

import com.kyumall.kyumallcommon.main.entity.Recommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {

}
