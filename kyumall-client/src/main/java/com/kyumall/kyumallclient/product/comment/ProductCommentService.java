package com.kyumall.kyumallclient.product.comment;

import com.kyumall.kyumallclient.product.comment.dto.CreateCommentRequest;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.repository.ProductCommentRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductCommentService {
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;
  private final ProductCommentRepository productCommentRepository;

  public Long createComment(Long productId, Long memberId, CreateCommentRequest request) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS));
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
    return productCommentRepository.save(ProductComment.builder()
            .product(product)
            .member(member)
            .content(request.getComment())
        .build()).getId();
  }
}
