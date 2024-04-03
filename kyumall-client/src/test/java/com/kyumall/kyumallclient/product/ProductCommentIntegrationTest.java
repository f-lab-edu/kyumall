package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.member.MemberFactory;
import com.kyumall.kyumallclient.product.comment.dto.CreateCommentRequest;
import com.kyumall.kyumallclient.product.comment.dto.ProductCommentDto;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.repository.ProductCommentRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("상품 댓글 통합테스트")
class ProductCommentIntegrationTest extends IntegrationTest {
  @Autowired
  MemberFactory memberFactory;
  @Autowired
  ProductFactory productFactory;
  @Autowired
  ProductCommentRepository productCommentRepository;
  Product apple;
  Product banana;
  private static final String password = "test1234";
  Member testMember1;

  @BeforeEach
  void dataInit() {
    // 물품
    Category testCategory = productFactory.createCategory("testCategory");
    apple = productFactory.createProduct("꿀사과", 1000);
    banana = productFactory.createProduct("바나나", 1000);
    // 회원
    testMember1 = memberFactory.createClient("test01", password);
  }

  @Test
  @DisplayName("상품 댓글 생성에 성공합니다.")
  void createProductComment_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    String comment = "상품에 대한 댓글입니다.";

    // when
    ExtractableResponse<Response> response = requestCreateComment(apple.getId(), comment, spec);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Long createdId = response.body().jsonPath().getLong("result.id");
    ProductComment createdEntity = productCommentRepository.findById(createdId)
        .orElseThrow(() -> new RuntimeException("test fail"));
    assertThat(createdEntity.getMember().getId()).isEqualTo(testMember1.getId());
    assertThat(createdEntity.getProduct().getId()).isEqualTo(apple.getId());
    assertThat(createdEntity.getContent()).isEqualTo(comment);
    assertThat(createdEntity.getParentComment()).isNull();
  }

  @Test
  @DisplayName("상품의 댓글 조회(첫번째 페이지) 성공합니다.")
  void getComments_first_page_success() {
    // given
    int batchSize = 10;
    int totalSize = 12;
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    List<Long> createdIDList = new ArrayList<>();
    for (int i = 0; i < totalSize; i++) {
      createdIDList.add(requestCreateCommentForGiven(apple.getId(), "test", spec));
    }

    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all().spec(spec)
        .pathParam("id", apple.getId())
        .when().get("/products/{id}/comments")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<ProductCommentDto> commentDtoList = response.body().jsonPath()
        .getList("result.content", ProductCommentDto.class);

    assertThat(commentDtoList).hasSize(batchSize);
    assertThat(commentDtoList).extracting("id")
        .isEqualTo(createdIDList.subList(0, batchSize));

    boolean isLastPage = response.body().jsonPath().getBoolean("result.last");
    assertThat(isLastPage).isFalse();  // 마지막 페이지 아님
  }

  @Test
  @DisplayName("상품의 댓글 조회(두번째 페이지)에 성공합니다.")
  void getComments_second_page_success() {
    // given
    int batchSize = 10;
    int page = 1; // 두번째 페이지
    int totalSize = 12; // 총 데이터 양
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    List<Long> createdIDList = new ArrayList<>();
    for (int i = 0; i < totalSize; i++) {
      createdIDList.add(requestCreateCommentForGiven(apple.getId(), "test", spec));
    }

    // when
    ExtractableResponse<Response> response = RestAssured.given().log().all().spec(spec)
        .pathParam("id", apple.getId())
        .queryParam("page", page)
        .when().get("/products/{id}/comments")
        .then().log().all()
        .extract();

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);

    List<ProductCommentDto> commentDtoList = response.body().jsonPath()
        .getList("result.content", ProductCommentDto.class);
    assertThat(commentDtoList).hasSize(totalSize - batchSize);  // 2
    assertThat(commentDtoList).extracting("id")
        .isEqualTo(createdIDList.subList(batchSize, createdIDList.size()));

    boolean isLastPage = response.body().jsonPath().getBoolean("result.last");
    assertThat(isLastPage).isTrue();  // 마지막 페이지
  }

  public static Long requestCreateCommentForGiven(
      Long productId, String comment, RequestSpecification spec) {
    ExtractableResponse<Response> response = requestCreateComment(productId, comment, spec);
    return response.body().jsonPath().getLong("result.id");
  }

  public static ExtractableResponse<Response> requestCreateComment(
      Long productId, String comment, RequestSpecification spec) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", productId)
        .contentType(ContentType.JSON)
        .body(new CreateCommentRequest(comment))
        .when().post("/products/{id}/comments")
        .then().log().all()
        .extract();
  }
}