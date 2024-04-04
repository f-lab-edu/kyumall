package com.kyumall.kyumallclient.product;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallclient.AuthTestUtil;
import com.kyumall.kyumallclient.IntegrationTest;
import com.kyumall.kyumallclient.member.MemberFactory;
import com.kyumall.kyumallclient.product.comment.dto.CreateCommentRequest;
import com.kyumall.kyumallclient.product.comment.dto.ProductCommentDto;
import com.kyumall.kyumallclient.product.comment.dto.UpdateCommentRequest;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.entity.ProductCommentRating;
import com.kyumall.kyumallcommon.product.repository.ProductCommentRatingRepository;
import com.kyumall.kyumallcommon.product.repository.ProductCommentRepository;
import com.kyumall.kyumallcommon.product.vo.RatingType;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  @Autowired
  ProductCommentRatingRepository productCommentRatingRepository;
  Product apple;
  Product banana;
  private static final String password = "test1234";
  Member testMember1;
  Member testMember2;

  @BeforeEach
  void dataInit() {
    // 물품
    Category testCategory = productFactory.createCategory("testCategory");
    apple = productFactory.createProduct("꿀사과", 1000);
    banana = productFactory.createProduct("바나나", 1000);
    // 회원
    testMember1 = memberFactory.createClient("test01", password);
    testMember2 = memberFactory.createClient("test02", password);
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
    ProductComment createdEntity = findComment(createdId);
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
      createdIDList.add(requestCreateCommentForGiven(apple.getId(), "test",
          testMember1.getUsername()));
    }

    // when
    ExtractableResponse<Response> response = requestGetComments(spec, apple.getId(), 0);

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

  private ExtractableResponse<Response> requestGetComments(RequestSpecification spec, Long productId, int page) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", productId)
        .queryParam("page", page)
        .when().get("/products/{id}/comments")
        .then().log().all()
        .extract();
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
      createdIDList.add(requestCreateCommentForGiven(apple.getId(), "test", testMember1.getUsername()));
    }

    // when
    ExtractableResponse<Response> response = requestGetComments(spec, apple.getId(), 1);

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

  @Test
  @DisplayName("상품의 댓글 목록을 조회합니다.(좋아요 숫자와 함께)")
  void getComments_with_likeCount_success() {
    // given
    // 댓글 추가
    Long comment1Id = requestCreateCommentForGiven(apple.getId(), "test", testMember1.getUsername());
    Long comment2Id = requestCreateCommentForGiven(apple.getId(), "test", testMember2.getUsername());
    // 댓글에 좋아요 추가
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(), password);
    requestUpdateCommentRating(spec, apple.getId() ,comment1Id, RatingType.LIKE);

    // when
    ExtractableResponse<Response> response = requestGetComments(spec, apple.getId(), 0);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    List<ProductCommentDto> commentDtoList = response.body().jsonPath()
        .getList("result.content", ProductCommentDto.class);
    assertThat(commentDtoList.get(0).getLikeCount()).isEqualTo(1);
    assertThat(commentDtoList.get(0).getDislikeCount()).isEqualTo(0);
    assertThat(commentDtoList.get(0).isLikeByCurrentUser()).isTrue();
    assertThat(commentDtoList.get(0).isDislikeByCurrentUser()).isFalse();
  }

  @Test
  @DisplayName("상품의 댓글을 수정합니다.")
  void updateComment_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    Long commentId = requestCreateCommentForGiven(apple.getId(), "첫 댓글입니다.", testMember1.getUsername());
    UpdateCommentRequest request = new UpdateCommentRequest("수정된 댓글 입니다.");

    // when
    ExtractableResponse<Response> response = requestUpdateComment(spec, apple.getId() ,commentId, request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    ProductComment updateComment = findComment(commentId);
    assertThat(updateComment.getContent()).isEqualTo(request.getComment());
  }

  @Test
  @DisplayName("상품 댓글의 작성자가 아니라서 수정에 실패합니다.")
  void updateComment_fail_because_not_writer() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    Long commentId = requestCreateCommentForGiven(apple.getId(), "첫 댓글입니다.", testMember1.getUsername());
    UpdateCommentRequest request = new UpdateCommentRequest("수정된 댓글 입니다.");
    RequestSpecification otherUserSpec = AuthTestUtil.requestLoginAndGetSpec(testMember2.getUsername(),
        password);

    // when
    ExtractableResponse<Response> response = requestUpdateComment(otherUserSpec, apple.getId() ,commentId, request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_FORBIDDEN);
  }

  @Test
  @DisplayName("상품 댓글과 상품의 ID가 맞지않아 수정에 실패합니다.")
  void updateComment_fail_because_product_not_match() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    Long commentId = requestCreateCommentForGiven(apple.getId(), "첫 댓글입니다.", testMember1.getUsername());
    UpdateCommentRequest request = new UpdateCommentRequest("수정된 댓글 입니다.");

    // when
    ExtractableResponse<Response> response = requestUpdateComment(spec, banana.getId() ,commentId, request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_BAD_REQUEST);
  }

  @Test
  @DisplayName("상품 댓글 삭제에 성공합니다.")
  void deleteComment_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    Long commentId = requestCreateCommentForGiven(apple.getId(), "첫 댓글입니다.", testMember1.getUsername());

    // when
    ExtractableResponse<Response> response = requestDeleteComment(spec, apple.getId() ,commentId);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    Optional<ProductComment> optionalComment = productCommentRepository.findById(commentId);
    assertThat(optionalComment.isEmpty()).isTrue();
  }

  @Test
  @DisplayName("상품 댓글에 좋아요를 합니다.")
  void updateCommentRating_good_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(),
        password);
    Long commentId = requestCreateCommentForGiven(apple.getId(), "첫 댓글입니다.", testMember1.getUsername());
    RatingType ratingType = RatingType.LIKE;

    // when
    ExtractableResponse<Response> response = requestUpdateCommentRating(spec, apple.getId() ,commentId, ratingType);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    ProductCommentRating rating = findCommentRating(commentId, testMember1.getId());
    assertThat(rating.getRatingType()).isEqualTo(ratingType);
  }

  @Test
  @DisplayName("상품의 댓글에 대댓글 생성을 성공합니다.")
  void createCommentReply_success() {
    // given
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(testMember1.getUsername(), password);
    Long commentId = requestCreateCommentForGiven(apple.getId(), "첫 댓글입니다.", testMember1.getUsername());
    CreateCommentRequest request = new CreateCommentRequest("대댓글 입니다.");

    // when
    ExtractableResponse<Response> response = requestCreateCommentReply(spec, apple.getId() ,commentId, request);

    // then
    assertThat(response.statusCode()).isEqualTo(HttpStatus.SC_OK);
    ProductComment comment = findComment(commentId);
    List<ProductComment> reply = productCommentRepository.findByParentComment(comment);
    assertThat(reply).hasSize(1);
    assertThat(reply.get(0).getContent()).isEqualTo(request.getComment());
  }

  public ExtractableResponse<Response> requestCreateCommentReply(RequestSpecification spec, Long productId ,
      Long commentId, CreateCommentRequest request) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", productId)
        .pathParam("commentId", commentId)
        .contentType(ContentType.JSON)
        .body(request)
        .when().post("/products/{id}/comments/{commentId}/reply")
        .then().log().all()
        .extract();
  }

  private ProductCommentRating findCommentRating(Long commentId, Long memberId) {
    return productCommentRatingRepository.findByProductComment_IdAndMember_Id(
        commentId, memberId).orElseThrow(() -> new RuntimeException("test fail"));
  }

  public ExtractableResponse<Response> requestUpdateCommentRating(RequestSpecification spec, Long productId ,
      Long commentId, RatingType ratingType) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", productId)
        .pathParam("commentId", commentId)
        .queryParam("ratingType", ratingType)
        .when().put("/products/{id}/comments/{commentId}/update-rating")
        .then().log().all()
        .extract();
  }

  public ExtractableResponse<Response> requestDeleteComment(RequestSpecification spec, Long productId ,Long commentId) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", productId)
        .pathParam("commentId", commentId)
        .when().delete("/products/{id}/comments/{commentId}/reply")
        .then().log().all()
        .extract();
  }

  public ExtractableResponse<Response> requestUpdateComment(RequestSpecification spec, Long productId ,Long commentId,
      UpdateCommentRequest request) {
    return RestAssured.given().log().all().spec(spec)
        .pathParam("id", productId)
        .pathParam("commentId", commentId)
        .contentType(ContentType.JSON)
        .body(request)
        .when().put("/products/{id}/comments/{commentId}")
        .then().log().all()
        .extract();
  }

  private ProductComment findComment(Long commentId) {
    return productCommentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("test fail"));
  }

  public static Long requestCreateCommentForGiven(Long productId, String comment, String username) {
    RequestSpecification spec = AuthTestUtil.requestLoginAndGetSpec(username, password);
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
