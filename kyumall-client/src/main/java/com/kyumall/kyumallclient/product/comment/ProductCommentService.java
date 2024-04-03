package com.kyumall.kyumallclient.product.comment;

import com.kyumall.kyumallclient.product.comment.dto.CreateCommentRequest;
import com.kyumall.kyumallclient.product.comment.dto.ProductCommentDto;
import com.kyumall.kyumallclient.product.comment.dto.UpdateCommentRequest;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.repository.ProductCommentRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ProductCommentService {
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;
  private final ProductCommentRepository productCommentRepository;

  public Long createComment(Long productId, Long memberId, CreateCommentRequest request) {
    Product product = findProductById(productId);
    Member member = findMember(memberId);
    return productCommentRepository.save(ProductComment.builder()
            .product(product)
            .member(member)
            .content(request.getComment())
        .build()).getId();
  }

  public Slice<ProductCommentDto> getComments(Long productId, Pageable pageable) {
    Product product = findProductById(productId);
    return productCommentRepository.findByProductOrderByCreatedAt(product, pageable).map(ProductCommentDto::from);
  }

  // 해당 댓글의 작성자만 수정가능
  @Transactional
  public void updateComment(Long productId, Long commentId, Long memberId,
      UpdateCommentRequest request) {
    ProductComment comment = findComment(commentId);
    if (!comment.getMember().getId().equals(memberId)) {
      throw new KyumallException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
    }
    if (!comment.getProduct().getId().equals(productId)) {
      throw new KyumallException(ErrorCode.COMMENT_AND_PRODUCT_NOT_MATCHED);
    }

    comment.updateComment(request.getComment());
  }

  private ProductComment findComment(Long commentId) {
    return productCommentRepository.findWithProductAndMemberById(commentId)
        .orElseThrow(() -> new KyumallException(ErrorCode.COMMENT_NOT_EXISTS));
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
  }

  private Product findProductById(Long productId) {
    return productRepository.findById(productId)
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS));
  }
}
