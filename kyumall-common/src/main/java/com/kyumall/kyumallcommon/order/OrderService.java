package com.kyumall.kyumallcommon.order;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.order.dto.CreateOrderRequest;
import com.kyumall.kyumallcommon.order.dto.ProductIdAndCount;
import com.kyumall.kyumallcommon.order.entity.OrderGroup;
import com.kyumall.kyumallcommon.pay.PayService;
import com.kyumall.kyumallcommon.order.entity.Orders;
import com.kyumall.kyumallcommon.order.repository.OrderGroupRepository;
import com.kyumall.kyumallcommon.order.entity.OrderStatus;
import com.kyumall.kyumallcommon.product.product.Product;
import com.kyumall.kyumallcommon.product.stock.ProductAndStockDto;
import com.kyumall.kyumallcommon.product.stock.Stock;
import com.kyumall.kyumallcommon.product.product.ProductRepository;
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
  private final OrderGroupRepository orderGroupRepository;
  private final PayService payService;
  private final StockRepository stockRepository;
  private final EntityManager em;

  @Transactional
  public Long createOrderGroup(Long memberId, CreateOrderRequest request) {
    Member member = findMember(memberId);
    List<Product> products = productRepository.findByIdIn(
        request.getProductIdAndCounts().stream().map(ProductIdAndCount::getProductId).toList());

    List<Integer> counts = request.getProductIdAndCounts().stream()
        .map(ProductIdAndCount::getCount).toList();

    OrderGroup order = OrderGroup.builder()
        .buyer(member)
        .orderStatus(OrderStatus.BEFORE_PAY)
        .orderDatetime(LocalDateTime.now())
        .build();
    order.addProducts(products, counts);
    return orderGroupRepository.save(order).getId();
  }

  private Member findMember(Long memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new KyumallException(ErrorCode.MEMBER_NOT_EXISTS));
  }

  @Transactional
  public void payOrder(Long payId, Long memberId) {
    OrderGroup order = findOrder(payId);
    // 재고 조회
//    List<Stock> stocks = findStock(order);
    List<ProductAndStockDto> productStocksDto = findProductStocks(order);
    // 재고 체크
    //validateStockQuantity(order, stocks);
    validateStockQuantityV2(order, productStocksDto);
    // 결재
    boolean isSuccess = payService.pay(order.getBuyer().getId(), order.calculateTotalPrice());
    if (!isSuccess) {
      throw new KyumallException(ErrorCode.ORDER_PAY_FAILS);
    }
//    for (Stock stock: stocks) {
//      em.detach(stock); // Stock 데이터를 Pessimistic Write Lock을 걸어 조회하기 위해 영속성 컨텍스트에서 detach 시키기 (1차 캐시에서 제거)
//    }
    // 재고 감소
    decreaseStocks(order, productStocksDto.stream().map(ProductAndStockDto::getStockId).toList());
    // 결재완료로 상태 변경
    order.payComplete();
  }

  private void decreaseStocks(OrderGroup order, List<Long> stockIds) {
    List<Stock> stocksToDecrease = stockRepository.findByInIdsWithPessimisticLock(stockIds);
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

  private List<ProductAndStockDto> findProductStocks(OrderGroup order) {
    return stockRepository.findProductStockByProductIdIn(
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

  private void validateStockQuantityV2(OrderGroup order, List<ProductAndStockDto> productStocks) {
    Map<Long, List<ProductAndStockDto>> groupByProductIdMap = productStocks.stream()
        .collect(Collectors.groupingBy(ps -> ps.getProductId()));

    for (Orders orders : order.getOrders()) {
      List<ProductAndStockDto> stocksByProduct = groupByProductIdMap.get(orders.getProduct().getId());
      if (stocksByProduct.isEmpty()) {
        throw new KyumallException(ErrorCode.STOCK_NOT_EXISTS);
      }
      ProductAndStockDto productStockDto = stocksByProduct.get(0);

      if (productStockDto.getQuantity() < orders.getCount()) {
        throw new KyumallException(ErrorCode.STOCK_IS_INSUFFICIENT);
      }
    }
  }

  private OrderGroup findOrder(Long payId) {
    return orderGroupRepository.findWithOrderItemsById(payId)
        .orElseThrow(() -> new KyumallException(ErrorCode.ORDER_NOT_EXISTS));
  }
}
