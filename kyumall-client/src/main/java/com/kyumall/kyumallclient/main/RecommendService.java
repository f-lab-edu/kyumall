package com.kyumall.kyumallclient.main;

import com.kyumall.kyumallclient.main.dto.RecommendationDto;
import com.kyumall.kyumallcommon.main.repository.RecommendationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecommendService {
  private final RecommendationRepository recommendationRepository;

  public List<RecommendationDto> getRecommendations() {
    return recommendationRepository.findAllInUse()
        .stream().map(RecommendationDto::from).toList();
  }
}
