package com.kyumall.kyumallcommon.product.cart;

import com.kyumall.kyumallcommon.product.cart.dto.AddCartItemRequest;
import com.kyumall.kyumallcommon.product.cart.dto.CartItemsDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.product.Product;
import com.kyumall.kyumallcommon.product.product.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
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
    Cart cart = findCartOrSaveByMember(member);
    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS));

    cart.addCartItem(product, request.getCount());
  }

  private Cart findCartOrSaveByMember(Member member) {
    return cartRepository.findWithItemsByMember(member)
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
    Cart cart = findCartOrSaveByMember(member);

    cart.deleteCartItems(cartItemIds);
  }

  /**
   * 카트에 담긴 상품 목록을 조회합니다.
   * @param memberId
   * @return
   */
  public List<CartItemsDto> getCartItems(Long memberId) {
    Member member = findMemberById(memberId);
    Cart cart = findCartOrSaveByMember(member);
    return cart.getCartItems()
        .stream().map(CartItemsDto::from).collect(Collectors.toList());
  }

  /**
   * 카트에 담긴 상품 갯수를 수정합니다.
   * @param memberId
   * @param id
   * @param count
   */
  @Transactional
  public void adjustCartItemCount(Long memberId, Long id, Integer count) {
    if (count <= 0) {
      throw new KyumallException(ErrorCode.ITEM_COUNT_MUST_BIGGER_THAN_ZERO);
    }
    Member member = findMemberById(memberId);
    Cart cart = findCartOrSaveByMember(member);

    CartItem cartItem = cart.getCartItem(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.CART_ITEM_NOT_EXISTS));
    cartItem.updateCount(count);
  }
}
