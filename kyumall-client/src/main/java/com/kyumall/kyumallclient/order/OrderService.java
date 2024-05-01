package com.kyumall.kyumallclient.order;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallclient.pay.PayService;
import com.kyumall.kyumallcommon.order.entity.OrderGroup;
import com.kyumall.kyumallcommon.order.entity.Orders;
import com.kyumall.kyumallcommon.order.repository.OrderRepository;
import com.kyumall.kyumallcommon.order.vo.OrderNumberGenerator;
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
  private final OrderNumberGenerator orderNumberGenerator;

  @Transactional
  public Long createOrderGroup(Long memberId, CreateOrderGroupRequest request) {
    Member member = findMember(memberId);
    List<Product> products = productRepository.findByIdIn(
        request.getProductIdAndCounts().stream().map(ProductIdAndCount::getProductId).toList());

    List<Integer> counts = request.getProductIdAndCounts().stream()
        .map(ProductIdAndCount::getCount).toList();

    OrderGroup orderGroup = OrderGroup.builder()
        .orderNumber(orderNumberGenerator.generate())
        .buyer(member)
        .orderDatetime(LocalDateTime.now())
        .build();
    orderGroup.addOrders(products, counts);
    return orderRepository.save(orderGroup).getId();
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
  }

  @Transactional
  public void payOrder(Long payId, Long memberId) {
    OrderGroup order = findOrderGroup(payId);
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

  private void decreaseStocks(OrderGroup order, List<Stock> stocks) {
    List<Stock> stocksToDecrease = stockRepository.findByInIdsWithPessimisticLock(
        stocks.stream().map(Stock::getId).toList());
    Map<Long, List<Orders>> orderItemByProductId = order.getOrders().stream()
        .collect(Collectors.groupingBy(oi -> oi.getProduct().getId()));

    for (Stock stock: stocksToDecrease) {
      Orders orders = orderItemByProductId.get(stock.getProduct().getId()).get(0);
      stock.decrease(orders.getCount());
    }
  }

  private List<Stock> findStock(OrderGroup order) {
    return stockRepository.findByProductIdIn(
        order.getOrders().stream().map(oi -> oi.getProduct().getId()).toList());
  }

  private void validateStockQuantity(OrderGroup order, List<Stock> stocks) {
    Map<Long, List<Stock>> stockMapByProduct = stocks.stream()
        .collect(Collectors.groupingBy(stock -> stock.getProduct().getId()));

    for (Orders orders : order.getOrders()) {
      List<Stock> stocksByProduct = stockMapByProduct.get(orders.getProduct().getId());
      if (stocksByProduct.isEmpty()) {
        throw new KyumallException(ErrorCode.STOCK_NOT_EXISTS);
      }
      Stock stock = stocksByProduct.get(0);

      if (!stock.decreasable(orders.getCount())) {
        throw new KyumallException(ErrorCode.STOCK_IS_INSUFFICIENT);
      }
    }
  }

  private OrderGroup findOrderGroup(Long payId) {
    return orderRepository.findWithOrdersById(payId)
        .orElseThrow(() -> new KyumallException(ErrorCode.ORDER_NOT_EXISTS));
  }
}
