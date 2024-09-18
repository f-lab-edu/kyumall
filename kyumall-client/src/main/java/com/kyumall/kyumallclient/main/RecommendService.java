package com.kyumall.kyumallclient.main;

import com.kyumall.kyumallclient.main.dto.RecommendationDto;
import com.kyumall.kyumallcommon.main.entity.Recommendation;
import com.kyumall.kyumallcommon.main.entity.RecommendationItem;
import com.kyumall.kyumallcommon.main.repository.RecommendationItemRepository;
import com.kyumall.kyumallcommon.main.repository.RecommendationRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecommendService {
  private final RecommendationRepository recommendationRepository;
  private final RecommendationItemRepository recommendationItemRepository;

  /**
   * 추천 목록을 조회합니다.
   * OneToMany 관계가 2개 존재하므로 (recommendation - recommendationItems, Product - ProductImage), 2개의 쿼리로 나누어 조죄합니다.
   * @return
   */
  public List<RecommendationDto> getRecommendations() {
    // 추천 목록 조회
    List<Recommendation> recommendations = recommendationRepository.findAllInUse();
    // 추천 아이템 목록 조회
    List<RecommendationItem> recommendationItems = recommendationItemRepository.findByRecommendationIdIn(
    recommendations);
    Map<Recommendation, List<RecommendationItem>> recommendationItemMap = recommendationItems.stream()
        .collect(Collectors.groupingBy(RecommendationItem::getRecommendation));

    recommendations.stream().forEach(recommend ->
        recommend.setRecommendationItems(recommendationItemMap.getOrDefault(recommend, new ArrayList<>())));

    return recommendations.stream().map(RecommendationDto::from).collect(Collectors.toList());
  }
}
