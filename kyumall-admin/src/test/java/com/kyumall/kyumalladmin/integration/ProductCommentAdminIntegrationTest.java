package com.kyumall.kyumalladmin.integration;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumalladmin.AuthTestUtil;
import com.kyumall.kyumalladmin.IntegrationTest;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.CategoryFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductCommentFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.product.repository.ProductCommentRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@DisplayName("상품 댓글 통합테스트")
public class ProductCommentAdminIntegrationTest extends IntegrationTest {
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private CategoryRepository categoryRepository;
  @Autowired
  private ProductCommentRepository productCommentRepository;

  Member adminMike;
  Member clientLee;
  Product apple;

  @BeforeEach
  void initData() { // 현재 테스트에 밀접한 연관이 없고, 공유되어도 문제될 것 없는 데이터만 BeforeEach 에서 생성
    adminMike = memberFactory.createMember(MemberFixture.MIKE);
    Member clientLee = memberFactory.createMember(MemberFixture.LEE);
    Category category = categoryRepository.saveAndFlush(CategoryFixture.FOOD.toEntity());
    apple = productRepository.saveAndFlush(ProductFixture.APPLE.toEntity(adminMike, category));
  }

  @Test
  @DisplayName("관리자가 본인의 상품에 등록된 댓글 삭제에 성공합니다.")
  void deleteCommentByAdmin_success() {
    ProductComment comment = productCommentRepository.save(
        ProductCommentFixture.GOOD.toEntity(apple, clientLee, null));
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(adminMike.getUsername(), MemberFixture.password);

    ExtractableResponse<Response> response = requestDeleteCommentByAdmin(spec, apple.getId(),
        comment.getId());

    assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    ProductComment deletedComment = productCommentRepository.findById(comment.getId()).orElseThrow();
    assertThat(deletedComment.isDeleted()).isTrue();
    assertThat(deletedComment.isDeletedByAdmin()).isTrue();
  }

  private static ExtractableResponse<Response> requestDeleteCommentByAdmin(RequestSpecification spec, Long productId, Long commentId) {
    ExtractableResponse<Response> response = RestAssured.given().log().all()
        .spec(spec)
        .pathParam("productId", productId)
        .pathParam("id", commentId)
        .when().delete("/products/{productId}/comments/{id}")
        .then().log().all()
        .extract();
    return response;
  }
}
