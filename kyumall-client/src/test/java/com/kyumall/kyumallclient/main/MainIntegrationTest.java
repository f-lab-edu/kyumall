package com.kyumall.kyumallclient.main;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.main.dto.BannerDto;
import com.kyumall.kyumallclient.main.dto.RecommendationDto;
import com.kyumall.kyumallclient.product.ProductFactory;
import com.kyumall.kyumallcommon.main.entity.Banner;
import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import com.kyumall.kyumallcommon.main.entity.Recommendation;
import com.kyumall.kyumallcommon.main.entity.RecommendationItem;
import com.kyumall.kyumallcommon.main.repository.BannerGroupRepository;
import com.kyumall.kyumallcommon.main.repository.BannerRepository;
import com.kyumall.kyumallcommon.main.repository.RecommendationItemRepository;
import com.kyumall.kyumallcommon.main.repository.RecommendationRepository;
import com.kyumall.kyumallcommon.upload.entity.Image;
import com.kyumall.kyumallcommon.upload.repository.ImageRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DisplayName("메인 페이지 통합테스트")
class MainIntegrationTest extends IntegrationTest {
  @Autowired
  BannerGroupRepository bannerGroupRepository;
  @Autowired
  BannerRepository bannerRepository;
  @Autowired
  RecommendationRepository recommendationRepository;
  @Autowired
  RecommendationItemRepository recommendationItemRepository;
  @Autowired
  ImageRepository imageRepository;
  @Autowired
  ProductFactory productFactory;

  String[] comparedBannerDtoFieldNames = new String[] {"id", "name", "url", "sortOrder"};
  String[] comparedRecommendationDtoFieldNames = new String[] {"title", "displayText"};
  String[] comparedRecommendationItemDtoFieldNames = new String[] {"itemName", "price"};
  Banner banner1;
  Banner banner2;
  Banner banner3;
  Recommendation recommendation1;
  Recommendation recommendation2;
  RecommendationItem recommendationItem1;
  RecommendationItem recommendationItem2;
  RecommendationItem recommendationItem3;
  RecommendationItem recommendationItem4;

  @Transactional
  @BeforeEach
  void initData() {
    Image testImage1 = imageRepository.save(Image.builder()
        .originalFileName("test image1.png")
        .storedFileName("aaa-bbb-ccc").build());

    // 배너 데이터 준비
    BannerGroup mainBannerGroup = bannerGroupRepository.save(BannerGroup.builder()
        .name("main")
        .description("메인 배너").build());
    banner1 = bannerRepository.save(Banner.builder()
        .bannerGroup(mainBannerGroup)
        .name("삼겹살 먹는 날")
        .url("/test1")
        .imageName(testImage1.getStoredFileName())
        .sortOrder(1).build());
    banner3 = bannerRepository.save(Banner.builder()
        .bannerGroup(mainBannerGroup)
        .name("2024 패션 위크")
        .url("/test2")
        .imageName(testImage1.getStoredFileName())
        .sortOrder(3).build());
    banner2 = bannerRepository.save(Banner.builder()
        .bannerGroup(mainBannerGroup)
        .name("특가 세일 상품")
        .url("/test3")
        .imageName(testImage1.getStoredFileName())
        .sortOrder(2).build());

    // 추천1 데이터 준비
    recommendation1 = recommendationRepository.save(Recommendation.builder()
        .title("오늘의 혜택")
        .displayText("지금 바로 확안하세요! 오늘의 혜택")
        .sortOrder(1)
        .inUse(true).build());
    recommendationItem1 = recommendationItemRepository.save(RecommendationItem.builder()
            .recommendation(recommendation1)
            .product(productFactory.createProduct("얼음골 사과", 30000))
            .sortOrder(1)
        .build());
    recommendationItem2 = recommendationItemRepository.save(RecommendationItem.builder()
        .recommendation(recommendation1)
        .product(productFactory.createProduct("과자 선물 세트", 30000))
        .sortOrder(2)
        .build());
    // 추천2
    recommendation2 = recommendationRepository.save(Recommendation.builder()
        .title("md 추천 패선")
        .displayText("MD의 추천 패션!")
        .sortOrder(2)
        .inUse(true).build());
    recommendationItem3 = recommendationItemRepository.save(RecommendationItem.builder()
        .recommendation(recommendation2)
        .product(productFactory.createProduct("스판 카고 팬츠", 30000))
        .sortOrder(1)
        .build());
    recommendationItem4 = recommendationItemRepository.save(RecommendationItem.builder()
        .recommendation(recommendation2)
        .product(productFactory.createProduct("스웨터", 30000))
        .sortOrder(2)
        .build());
  }

  @Test
  @DisplayName("메인 페이지 데이터 조회에 성공합니다.")
  void getMain_success() {
    // given
    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .when().get("/main")
        .then().log().all()
        .extract();
    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<BannerDto> banners = response.body().jsonPath().getList("result.banners", BannerDto.class);
    assertThat(banners).hasSize(3);
    assertThat(banners.get(0)).usingRecursiveComparison().comparingOnlyFields(comparedBannerDtoFieldNames)
        .isEqualTo(banner1);
    assertThat(banners.get(1)).usingRecursiveComparison().comparingOnlyFields(comparedBannerDtoFieldNames)
        .isEqualTo(banner2);
    assertThat(banners.get(2)).usingRecursiveComparison().comparingOnlyFields(comparedBannerDtoFieldNames)
        .isEqualTo(banner3);

    List<RecommendationDto> recommendations = response.body().jsonPath().getList("result.recommendations", RecommendationDto.class);
    assertThat(recommendations).hasSize(2);
    // 추천1
    RecommendationDto recommendationDto1 = recommendations.get(0);
    assertThat(recommendationDto1).usingRecursiveComparison().comparingOnlyFields(comparedRecommendationDtoFieldNames)
        .isEqualTo(recommendation1);
    assertThat(recommendationDto1.getRecommendationItemDtos().get(0)).usingRecursiveComparison().comparingOnlyFields(comparedRecommendationItemDtoFieldNames)
        .isEqualTo(recommendationItem1);
    assertThat(recommendationDto1.getRecommendationItemDtos().get(1)).usingRecursiveComparison().comparingOnlyFields(comparedRecommendationItemDtoFieldNames)
        .isEqualTo(recommendationItem2);
    // 추천2
    RecommendationDto recommendationDto2 = recommendations.get(1);
    assertThat(recommendationDto2).usingRecursiveComparison().comparingOnlyFields(comparedRecommendationDtoFieldNames)
        .isEqualTo(recommendation2);
    assertThat(recommendationDto2.getRecommendationItemDtos().get(0)).usingRecursiveComparison().comparingOnlyFields(comparedRecommendationItemDtoFieldNames)
        .isEqualTo(recommendationItem3);
    assertThat(recommendationDto2.getRecommendationItemDtos().get(1)).usingRecursiveComparison().comparingOnlyFields(comparedRecommendationItemDtoFieldNames)
        .isEqualTo(recommendationItem4);

  }
}
