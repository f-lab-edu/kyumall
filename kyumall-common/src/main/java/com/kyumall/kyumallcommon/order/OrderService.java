package com.kyumall.kyumallcommon.order;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.order.dto.CreateOrderRequest;
import com.kyumall.kyumallcommon.order.dto.ProductIdAndCount;
import com.kyumall.kyumallcommon.order.entity.Orders;
import com.kyumall.kyumallcommon.pay.PayService;
import com.kyumall.kyumallcommon.order.entity.OrderItem;
import com.kyumall.kyumallcommon.order.repository.OrdersRepository;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import com.kyumall.kyumallcommon.product.stock.ProductAndStockDto;
import com.kyumall.kyumallcommon.product.stock.Stock;
import com.kyumall.kyumallcommon.product.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.stock.StockRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OrderService {
  private final MemberRepository memberRepository;
  private final ProductRepository productRepository;
  private final OrdersRepository ordersRepository;
  private final PayService payService;
  private final StockRepository stockRepository;
  private final EntityManager em;

  /**
   * 주문 생성
   * 주문하는 상품과 갯수에 맞는 주문을 생성합니다.
   * @param memberId
   * @param request
   * @return
   */
  @Transactional
  public Long createOrder(Long memberId, CreateOrderRequest request) {
    Member member = findMember(memberId);
    List<Product> products = productRepository.findByIdIn(
        request.getProductIdAndCounts().stream().map(ProductIdAndCount::getProductId).toList());

    List<Integer> counts = request.getProductIdAndCounts().stream()
        .map(ProductIdAndCount::getCount).toList();

    Orders order = Orders.builder()
        .buyer(member)
        .orderDatetime(LocalDateTime.now())
        .build();
    order.addProducts(products, counts);
    return ordersRepository.save(order).getId();
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
  }

  /**
   * 주문 결제
   * @param payId
   * @param memberId
   */
  @Transactional
  public void payOrder(Long payId, Long memberId) {
    Orders orders = findOrder(payId);
    // 재고 조회
    List<ProductAndStockDto> productStocksDtos = findProductStocksDto(orders);
    // 재고 체크
    validateStockQuantity(orders, productStocksDtos);
    // 결재
    boolean isSuccess = payService.pay(orders.getBuyer().getId(), orders.calculateTotalPrice());
    if (!isSuccess) {
      throw new KyumallException(ErrorCode.ORDER_PAY_FAILS);
    }
    // 재고 감소
    decreaseStocks(orders, productStocksDtos.stream().map(ProductAndStockDto::getStockId).toList());
    // 결재완료로 상태 변경
    orders.getOrderItems()
        .stream().forEach(orderItem -> orderItem.payComplete());
  }

  private void decreaseStocks(Orders order, List<Long> stockIds) {
    List<Stock> stocksToDecrease = stockRepository.findByInIdsWithPessimisticLock(stockIds);
    Map<Long, List<OrderItem>> orderItemByProductId = order.getOrderItems().stream()
        .collect(Collectors.groupingBy(oi -> oi.getProduct().getId()));

    for (Stock stock: stocksToDecrease) {
      OrderItem orderItem = orderItemByProductId.get(stock.getProduct().getId()).get(0);
      stock.decrease(orderItem.getCount());
    }
  }

  /**
   * 주문의 상품과 재고 ID를 조회합니다.
   * Stock 객체를 조회하면 1차 캐시에 저장되어 향후 재고 감소를 위해 Pessimistic Lock으로 Stock을 조회하여도 쿼리가 발생하지 않고, 1차 캐시의 Stock 객체를 사용하기 때문에 dto를 조회하도록 변경하였습니다.
   * entityManager.detach 로 stock을 직접 1차 캐시에서 제거하는 방식에서 entityManager를 직접 사용하지 않도록 변경된 방식입니다.
   * @param order
   * @return
   */
  private List<ProductAndStockDto> findProductStocksDto(Orders order) {
    return stockRepository.findProductStockByProductIdIn(
        order.getOrderItems().stream().map(oi -> oi.getProduct().getId()).toList());
  }

  private void validateStockQuantity(Orders order, List<ProductAndStockDto> productStocks) {
    Map<Long, List<ProductAndStockDto>> groupByProductIdMap = productStocks.stream()
        .collect(Collectors.groupingBy(ps -> ps.getProductId()));

    for (OrderItem orderItem : order.getOrderItems()) {
      List<ProductAndStockDto> stocksByProduct = groupByProductIdMap.get(orderItem.getProduct().getId());
      if (stocksByProduct.isEmpty()) {
        throw new KyumallException(ErrorCode.STOCK_NOT_EXISTS);
      }
      ProductAndStockDto productStockDto = stocksByProduct.get(0);

      if (productStockDto.getQuantity() < orderItem.getCount()) {
        throw new KyumallException(ErrorCode.STOCK_IS_INSUFFICIENT);
      }
    }
  }

  private Orders findOrder(Long payId) {
    return ordersRepository.findWithOrderItemsById(payId)
        .orElseThrow(() -> new KyumallException(ErrorCode.ORDER_NOT_EXISTS));
  }
}
