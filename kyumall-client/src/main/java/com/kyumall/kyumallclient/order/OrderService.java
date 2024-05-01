package com.kyumall.kyumallclient.order;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallclient.pay.PayService;
import com.kyumall.kyumallcommon.order.entity.OrderItem;
import com.kyumall.kyumallcommon.order.entity.Orders;
import com.kyumall.kyumallcommon.order.repository.OrderRepository;
import com.kyumall.kyumallcommon.order.vo.OrderStatus;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.Stock;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import com.kyumall.kyumallcommon.product.repository.StockRepository;
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
  private final OrderRepository orderRepository;
  private final PayService payService;
  private final StockRepository stockRepository;
  private final EntityManager em;

  @Transactional
  public Long createOrder(Long memberId, CreateOrderRequest request) {
    Member member = findMember(memberId);
    List<Product> products = productRepository.findByIdIn(
        request.getProductIdAndCounts().stream().map(ProductIdAndCount::getProductId).toList());

    List<Integer> counts = request.getProductIdAndCounts().stream()
        .map(ProductIdAndCount::getCount).toList();

    Orders order = Orders.builder()
        .buyer(member)
        .orderStatus(OrderStatus.BEFORE_PAY)
        .orderDatetime(LocalDateTime.now())
        .build();
    order.addProducts(products, counts);
    return orderRepository.save(order).getId();
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
  }

  @Transactional
  public void payOrder(Long payId, Long memberId) {
    Orders order = findOrder(payId);
    // 재고 조회
    List<Stock> stocks = findStock(order);
    // 재고 체크
    validateStockQuantity(order, stocks);
    // 결재
    boolean isSuccess = payService.pay(order.getBuyer().getId(), order.calculateTotalPrice());
    if (!isSuccess) {
      throw new KyumallException(ErrorCode.ORDER_PAY_FAILS);
    }
    for (Stock stock: stocks) {
      em.detach(stock); // Stock 데이터를 Pessimistic Write Lock을 걸어 조회하기 위해 영속성 컨텍스트에서 detach 시키기 (1차 캐시에서 제거)
    }
    // 재고 감소
    decreaseStocks(order, stocks);
    // 결재완료로 상태 변경
    order.payComplete();
  }

  private void decreaseStocks(Orders order, List<Stock> stocks) {
    List<Stock> stocksToDecrease = stockRepository.findByInIdsWithPessimisticLock(
        stocks.stream().map(Stock::getId).toList());
    Map<Long, List<OrderItem>> orderItemByProductId = order.getOrderItems().stream()
        .collect(Collectors.groupingBy(oi -> oi.getProduct().getId()));

    for (Stock stock: stocksToDecrease) {
      OrderItem orderItem = orderItemByProductId.get(stock.getProduct().getId()).get(0);
      stock.decrease(orderItem.getCount());
    }
  }

  private List<Stock> findStock(Orders order) {
    return stockRepository.findByProductIdIn(
        order.getOrderItems().stream().map(oi -> oi.getProduct().getId()).toList());
  }

  private void validateStockQuantity(Orders order, List<Stock> stocks) {
    Map<Long, List<Stock>> stockMapByProduct = stocks.stream()
        .collect(Collectors.groupingBy(stock -> stock.getProduct().getId()));

    for (OrderItem orderItem: order.getOrderItems()) {
      List<Stock> stocksByProduct = stockMapByProduct.get(orderItem.getProduct().getId());
      if (stocksByProduct.isEmpty()) {
        throw new KyumallException(ErrorCode.STOCK_NOT_EXISTS);
      }
      Stock stock = stocksByProduct.get(0);

      if (!stock.decreasable(orderItem.getCount())) {
        throw new KyumallException(ErrorCode.STOCK_IS_INSUFFICIENT);
      }
    }
  }

  private Orders findOrder(Long payId) {
    return orderRepository.findWithOrderItemsById(payId)
        .orElseThrow(() -> new KyumallException(ErrorCode.ORDER_NOT_EXISTS));
  }
}
