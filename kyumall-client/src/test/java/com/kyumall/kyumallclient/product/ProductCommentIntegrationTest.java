package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.member.MemberFactory;
import com.kyumall.kyumallclient.product.comment.dto.CreateCommentRequest;
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
