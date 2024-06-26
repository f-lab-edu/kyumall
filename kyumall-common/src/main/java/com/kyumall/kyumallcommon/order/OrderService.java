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
    OrderGroup orderGroup = findOrder(payId);
    // 재고 조회
    List<ProductAndStockDto> productStocksDtos = findProductStocksDto(orderGroup);
    // 재고 체크
    validateStockQuantity(orderGroup, productStocksDtos);
    // 결재
    boolean isSuccess = payService.pay(orderGroup.getBuyer().getId(), orderGroup.calculateTotalPrice());
    if (!isSuccess) {
      throw new KyumallException(ErrorCode.ORDER_PAY_FAILS);
    }
    // 재고 감소
    decreaseStocks(orderGroup, productStocksDtos.stream().map(ProductAndStockDto::getStockId).toList());
    // 결재완료로 상태 변경
    orderGroup.getOrders()
        .stream().forEach(orders -> orders.payComplete());
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

  /**
   * 주문의 상품과 재고 ID를 조회합니다.
   * Stock 객체를 조회하면 1차 캐시에 저장되어 향후 재고 감소를 위해 Pessimistic Lock으로 Stock을 조회하여도 쿼리가 발생하지 않고, 1차 캐시의 Stock 객체를 사용하기 때문에 dto를 조회하도록 변경하였습니다.
   * entityManager.detach 로 stock을 직접 1차 캐시에서 제거하는 방식에서 entityManager를 직접 사용하지 않도록 변경된 방식입니다.
   * @param order
   * @return
   */
  private List<ProductAndStockDto> findProductStocksDto(OrderGroup order) {
    return stockRepository.findProductStockByProductIdIn(
        order.getOrders().stream().map(oi -> oi.getProduct().getId()).toList());
  }

  private void validateStockQuantity(OrderGroup order, List<ProductAndStockDto> productStocks) {
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
