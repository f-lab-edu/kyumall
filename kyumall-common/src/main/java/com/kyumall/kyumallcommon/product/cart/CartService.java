package com.kyumall.kyumallcommon.product.cart;

import com.kyumall.kyumallcommon.product.cart.dto.AddCartItemRequest;
import com.kyumall.kyumallcommon.product.cart.dto.CartItemsDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CartService {
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final MemberRepository memberRepository;

  /**
   * 카트에 상품을 추가합니다.
   * @param memberId
   * @param request
   */
  @Transactional
  public void addCartItem(Long memberId, AddCartItemRequest request) {
    Member member = findMemberById(memberId);
    Cart cart = findCartWithCarItemsByMemberOrSaveIfNull(member);
    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS));

    cart.addCartItem(product, request.getCount());
  }

  private Cart findCartWithCarItemsByMemberOrSaveIfNull(Member member) {
    return cartRepository.findWithItemsByMember(member)
        .orElseGet(() -> cartRepository.save(new Cart(member)));
  }

  private Cart findCartByMemberOrSaveIfNull(Member member) {
    return cartRepository.findByMember(member)
        .orElseGet(() -> cartRepository.save(new Cart(member)));
  }

  private Member findMemberById(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
  }

  /**
   * 카트에 담긴 상품을 삭제합니다.
   * @param memberId
   * @param cartItemIds
   */
  @Transactional
  public void deleteCartItem(Long memberId, List<Long> cartItemIds) {
    Member member = findMemberById(memberId);
    Cart cart = findCartWithCarItemsByMemberOrSaveIfNull(member);

    cart.deleteCartItems(cartItemIds);
  }

  /**
   * 카트에 담긴 상품 목록을 조회합니다.
   * OneToMany 관계가 두개 이므로(Cart - CartItems, Product - ProductImages), 두번의 쿼리로 따로 조회합니다.
   * @param memberId
   * @return
   */
  public List<CartItemsDto> getCartItems(Long memberId) {
    Member member = findMemberById(memberId);

    Cart cart = findCartByMemberOrSaveIfNull(member);
    List<CartItem> cartItemsWithImage = cartItemRepository.findWithProductImageByCart(cart);
    return cartItemsWithImage.stream()
            .map(CartItemsDto::from).collect(Collectors.toList());
  }

  /**
   * 카트에 담긴 상품 갯수를 수정합니다.
   * @param memberId
   * @param cartItemId
   * @param count
   */
  @Transactional
  public void adjustCartItemCount(Long memberId, Long cartItemId, Integer count) {
    if (count <= 0) {
      throw new KyumallException(ErrorCode.ITEM_COUNT_MUST_BIGGER_THAN_ZERO);
    }
    Member member = findMemberById(memberId);
    Cart cart = findCartWithCarItemsByMemberOrSaveIfNull(member);

    cart.updateCartItemCount(cartItemId, count);
  }
}
