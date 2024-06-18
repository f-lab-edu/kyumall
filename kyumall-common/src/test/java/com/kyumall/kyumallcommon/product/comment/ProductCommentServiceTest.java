package com.kyumall.kyumallcommon.product.comment;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.JpaRepositoryTest;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductCommentFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductCommentRatingFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductFixture;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.comment.dto.ProductCommentDto;
import com.kyumall.kyumallcommon.product.product.Product;
import com.kyumall.kyumallcommon.product.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;

class ProductCommentServiceTest extends JpaRepositoryTest {
  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private MemberRepository memberRepository;
  @Autowired
  private ProductCommentRepository productCommentRepository;
  @Autowired
  private ProductCommentRatingRepository productCommentRatingRepository;
  @Autowired
  private MemberFactory memberFactory;
  @Autowired
  private ProductFactory productFactory;

  private ProductCommentService productCommentService;
  private Member client;
  private Member client2;
  private Member seller;
  private Product product;

  private int dataSize = 100;
  private int pageSize = 10;

  @BeforeEach
  void init() {
    productCommentService = new ProductCommentService(productRepository, memberRepository,
        productCommentRepository, productCommentRatingRepository);

    client = memberFactory.createMember(MemberFixture.KIM);
    client2 = memberFactory.createMember(MemberFixture.PARK);
    seller = memberFactory.createMember(MemberFixture.KIM);

    product = productFactory.createProduct(ProductFixture.APPLE, seller);

    for (int i = 0; i < dataSize; i++) {
      ProductComment comment = productCommentRepository.save(ProductCommentFixture.GOOD.toEntity(product, client, null));
      ProductComment subComment1 = productCommentRepository.save(ProductCommentFixture.THANKS.toEntity(product, client, comment));
      ProductComment subComment2 = productCommentRepository.save(ProductCommentFixture.HELP.toEntity(product, client, comment));

      productCommentRatingRepository.save(ProductCommentRatingFixture.LIKE.toEntity(comment, client));
      productCommentRatingRepository.save(ProductCommentRatingFixture.LIKE.toEntity(comment, client2));
    }
  }

  @Test
  void 상품_댓글_조회_IN_쿼리로_조합_하는_경우() {
    // given

    // when
    long startTime = System.currentTimeMillis();
    Slice<ProductCommentDto> comments = productCommentService.getComments(product.getId(),
        PageRequest.of(0, pageSize),
        AuthenticatedUser.from(client));
    long endTIme = System.currentTimeMillis();

    // then
    System.out.println("### Execution time: " + (endTIme - startTime) + " ms");
    assertThat(comments).hasSize(pageSize);
  }

  @Test
  void 상품_댓글_조회_서브쿼리_사용하는_경우() {
    // given
    // when
    long startTime = System.currentTimeMillis();
    Slice<ProductCommentDto> comments = productCommentService.getCommentsV2(product.getId(),
        PageRequest.of(0, pageSize),
        AuthenticatedUser.from(client));
    long endTIme = System.currentTimeMillis();

    // then
    System.out.println("### Execution time: " + (endTIme - startTime) + " ms");
    assertThat(comments).hasSize(pageSize);
  }
  @Test
  void 상품_댓글_조회_조인을_사용하는_경우() {
    // given
    // when
    long startTime = System.currentTimeMillis();
    Slice<ProductCommentDto> comments = productCommentService.getCommentsV3(product.getId(),
        PageRequest.of(0, pageSize),
        AuthenticatedUser.from(client));
    long endTIme = System.currentTimeMillis();

    // then
    System.out.println("### Execution time: " + (endTIme - startTime) + " ms");
    assertThat(comments).hasSize(pageSize);
  }

}
