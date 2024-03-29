package com.kyumall.kyumallclient.product.cart;

import com.kyumall.kyumallclient.product.cart.dto.CartItemsDto;
import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.product.entity.Cart;
import com.kyumall.kyumallcommon.product.entity.CartItem;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.CartItemRepository;
import com.kyumall.kyumallcommon.product.repository.CartRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
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

  @Transactional
  public void addCartItem(Long memberId, AddCartItemRequest request) {
    Member member = findMemberWithCart(memberId);
    addCartIfNotExists(member);

    Product product = productRepository.findById(request.getProductId())
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS));

    Optional<CartItem> cartItem = member.getCart().getCartItemByProduct(product);
    if (cartItem.isPresent()) {
      cartItem.get().plusCount(request.getCount());
      return;
    }

    member.getCart().addCart(product, request.getCount());
  }

  private void addCartIfNotExists(Member member) {
    if (member.hasCart()) {
      return;
    }
    Cart newCart = cartRepository.save(
        Cart.builder()
            .member(member).build());
    member.setCart(newCart);
  }

  private Member findMemberWithCart(Long memberId) {
    return memberRepository.findWithCartById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
  }

  @Transactional
  public void deleteCartItem(Long memberId, List<Long> cartItemIds) {
    Member member = findMemberWithCart(memberId);

    member.getCart().deleteCartItems(cartItemIds);
  }

  public List<CartItemsDto> getCartItems(Long memberId) {
    Member member = findMemberWithCart(memberId);
    return member.getCart().getCartItems()
        .stream().map(CartItemsDto::from).collect(Collectors.toList());
  }

  @Transactional
  public void adjustCount(Long memberId, Long id, Integer count) {
    if (count <= 0) {
      throw new KyumallException(ErrorCode.ITEM_COUNT_MUST_BIGGER_THAN_ZERO);
    }

    Member member = findMemberWithCart(memberId);
    CartItem cartItem = member.getCart().getCartItem(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.CART_ITEM_NOT_EXISTS));
    cartItem.updateCount(count);
  }
}
