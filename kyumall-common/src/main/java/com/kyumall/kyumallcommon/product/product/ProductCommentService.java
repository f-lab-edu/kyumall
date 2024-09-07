package com.kyumall.kyumallcommon.product.product;

import com.kyumall.kyumallcommon.product.product.dto.CreateCommentRequest;
import com.kyumall.kyumallcommon.product.product.dto.ProductCommentDto;
import com.kyumall.kyumallcommon.product.product.dto.UpdateCommentRequest;
import com.kyumall.kyumallcommon.auth.authentication.AuthenticatedUser;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.product.dto.LikeCountDto;
import com.kyumall.kyumallcommon.product.product.dto.ReplyCountDto;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.entity.RatingType;
import com.kyumall.kyumallcommon.product.product.repository.ProductCommentRatingRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductCommentRepository;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.product.entity.ProductComment;
import com.kyumall.kyumallcommon.product.product.entity.ProductCommentRating;
import java.util.List;
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
  private final ProductCommentRatingRepository productCommentRatingRepository;

  public Long createComment(Long productId, Long memberId, CreateCommentRequest request) {
    Product product = findProductById(productId);
    Member member = findMember(memberId);
    return productCommentRepository.save(ProductComment.builder()
            .product(product)
            .member(member)
            .content(request.getComment())
        .build()).getId();
  }

  public Slice<ProductCommentDto> getComments(Long productId, Pageable pageable,
      AuthenticatedUser authenticatedUser) {
    Product product = findProductById(productId);
    // 상품 댓글 조회
    Slice<ProductCommentDto> commentDtos = productCommentRepository.findByProductOrderByCreatedAt(product,
        pageable).map(ProductCommentDto::from);
    // 상품 댓글 ID 만 추출
    List<Long> commentIds = extractCommetIdList(commentDtos);

    // 댓글 좋아요, 싫어요 수 조회
    List<LikeCountDto> ratingCounts = findCommentRatings(authenticatedUser, commentIds);

    // 대댓글 갯수 조회
    List<ReplyCountDto> replyCounts = productCommentRepository.findReplyCountInCommentId(commentIds);

    // 댓글에 좋아요수, 대댓글수 세팅
    commentDtos.forEach(commentDto -> {
      // 조회한 좋아요, 싫어요 수 세팅
      setLikeCounts(ratingCounts, commentDto);

      // 대댓글수 세팅
      ReplyCountDto replyCountDto = replyCounts.stream()
          .filter(replyCount -> replyCount.getParentCommentId().equals(commentDto.getId()))
          .findFirst()
          .orElseGet(ReplyCountDto::createZeroCount);
      commentDto.setReplyCount(replyCountDto.getReplyCount());
    });

    return commentDtos;
  }

  public Slice<ProductCommentDto> getCommentsV2(Long productId, Pageable pageable, AuthenticatedUser authenticatedUser) {
    Product product = findProductById(productId);
    long memberId = authenticatedUser == null ? 0 : authenticatedUser.getMemberId();

    return productCommentRepository.findCommentDtosUsingSubquery(product, memberId, pageable);
  }

  public Slice<ProductCommentDto> getCommentsV3(Long productId, Pageable pageable, AuthenticatedUser authenticatedUser) {
    Product product = findProductById(productId);
    long memberId = authenticatedUser == null ? 0 : authenticatedUser.getMemberId();

    return productCommentRepository.findCommentDtosUsingJoin(product, memberId, pageable);
  }

  private static void setLikeCounts(List<LikeCountDto> ratingCounts, ProductCommentDto commentDto) {
    LikeCountDto likeCountDto = ratingCounts.stream()
        .filter(ratingCount -> ratingCount.getProductCommentId().equals(commentDto.getId()))
        .findFirst()
        .orElseGet(LikeCountDto::createZeroCount);
    commentDto.setRatingCount(likeCountDto);
  }

  private static List<Long> extractCommetIdList(Slice<ProductCommentDto> commentDtos) {
    return commentDtos.stream().map(ProductCommentDto::getId).toList();
  }

  private List<LikeCountDto> findCommentRatings(AuthenticatedUser authenticatedUser, List<Long> commentIds) {
    Long memberId = (authenticatedUser == null) ? 0 : authenticatedUser.getMemberId();
    return productCommentRatingRepository.findRatingCountInCommentIds(commentIds, memberId);
  }

  // 해당 댓글의 작성자만 수정가능
  @Transactional
  public void updateComment(Long productId, Long commentId, Long memberId,
      UpdateCommentRequest request) {
    ProductComment comment = findComment(commentId);
    validateUpdateComment(productId, memberId, comment);

    comment.updateComment(request.getComment());
  }

  // 삭제
  public void deleteComment(Long productId, Long commentId, Long memberId) {
    ProductComment comment = findComment(commentId);
    validateUpdateComment(productId, memberId, comment);

    productCommentRepository.deleteById(comment.getId());
  }

  @Transactional
  public void updateCommentRating(Long productId, Long commentId, Long memberId, RatingType ratingType) {
    ProductComment comment = findComment(commentId);
    Member member = findMember(memberId);
    validateUpdateComment(productId, memberId, comment);

    ProductCommentRating rating = productCommentRatingRepository.findByProductCommentAndMember(comment, member)
        .orElseGet(() -> ProductCommentRating.builder()
              .productComment(comment)
              .member(member)
              .ratingType(RatingType.NONE)
              .build()
        );
    rating.updateRating(ratingType);
    productCommentRatingRepository.save(rating);
  }

  // 대댓글 생성
  public Long createCommentReply(Long productId, Long commentId, Long memberId, CreateCommentRequest request) {
    Member member = findMember(memberId);
    ProductComment comment = findComment(commentId);
    if (!comment.getProduct().getId().equals(productId)) {
      throw new KyumallException(ErrorCode.COMMENT_AND_PRODUCT_NOT_MATCHED);
    }
    return productCommentRepository.save(ProductComment.builder()
        .member(member)
        .parentComment(comment)
        .content(request.getComment())
        .build()).getId();
  }

  // 대댓글 조회
  public Slice<ProductCommentDto> getCommentReplies(Long productId, Long commentId, AuthenticatedUser authenticatedUser,
      Pageable pageable) {
    ProductComment parentComment = findComment(commentId);

    Slice<ProductCommentDto> commentDtos = productCommentRepository.findByParentCommentOrderByCreatedAt(parentComment, pageable)
        .map(ProductCommentDto::from);
    // Comment ID 추출
    List<Long> commentIds = extractCommetIdList(commentDtos);
    // 댓글 좋아요, 싫어요 수 조회
    List<LikeCountDto> ratingCounts = findCommentRatings(authenticatedUser, commentIds);
    // 좋아요수 세팅
    commentDtos.forEach(commentDto -> setLikeCounts(ratingCounts, commentDto));

    return commentDtos;
  }

  private static void validateUpdateComment(Long productId, Long memberId, ProductComment comment) {
    if (!comment.getMember().getId().equals(memberId)) {
      throw new KyumallException(ErrorCode.COMMENT_UPDATE_FORBIDDEN);
    }
    if (!comment.getProduct().getId().equals(productId)) {
      throw new KyumallException(ErrorCode.COMMENT_AND_PRODUCT_NOT_MATCHED);
    }
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
