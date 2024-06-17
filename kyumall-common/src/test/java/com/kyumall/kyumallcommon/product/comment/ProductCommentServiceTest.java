package com.kyumall.kyumallcommon.product.comment;

import static org.assertj.core.api.Assertions.*;

import com.kyumall.kyumallcommon.JpaRepositoryTest;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.factory.MemberFactory;
import com.kyumall.kyumallcommon.factory.ProductFactory;
import com.kyumall.kyumallcommon.fixture.member.MemberFixture;
import com.kyumall.kyumallcommon.fixture.product.ProductCommentFixture;
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
  private Member seller;
  private Product product;

  @BeforeEach
  void init() {
    productCommentService = new ProductCommentService(productRepository, memberRepository,
        productCommentRepository, productCommentRatingRepository);

    client = memberFactory.createMember(MemberFixture.KIM);
    seller = memberFactory.createMember(MemberFixture.KIM);

    product = productFactory.createProduct(ProductFixture.APPLE, seller);
  }

  @Test
  void 상품_댓글_조회_IN_쿼리로_조합_하는_경우() {
    // given
    ProductComment comment1 = productCommentRepository.save(ProductCommentFixture.GOOD.toEntity(product, client, null));
    ProductComment subComment1 = productCommentRepository.save(ProductCommentFixture.THANKS.toEntity(product, client, comment1));
    ProductComment subComment2 = productCommentRepository.save(ProductCommentFixture.HELP.toEntity(product, client, comment1));

    ProductComment comment2 = productCommentRepository.save(ProductCommentFixture.GOOD.toEntity(product, client, null));
    ProductComment subComment3 = productCommentRepository.save(ProductCommentFixture.THANKS.toEntity(product, client, comment2));

    // when
    long startTime = System.currentTimeMillis();
    Slice<ProductCommentDto> comments = productCommentService.getComments(product.getId(),
        PageRequest.of(0, 10),
        AuthenticatedUser.from(client));
    long endTIme = System.currentTimeMillis();

    // then
    System.out.println("### Execution time: " + (endTIme - startTime) + " ms");
    assertThat(comments).hasSize(2);
  }

}
