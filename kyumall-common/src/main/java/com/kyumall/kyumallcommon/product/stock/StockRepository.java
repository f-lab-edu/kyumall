package com.kyumall.kyumallcommon.product.stock;

import com.kyumall.kyumallcommon.product.product.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface StockRepository extends JpaRepository<Stock, Long> {
  Optional<Stock> findByProduct(Product product);

  @Query("select s from Stock s where s.product.id in :productIds")
  List<Stock> findByProductIdIn(List<Long> productIds);

  @Query("""  
    select new com.kyumall.kyumallcommon.product.stock.ProductAndStockDto(s.product.id, s.id ,s.quantity)
    from Stock s
    join s.product p
    where s.product.id in :productIds
  """)
  List<ProductAndStockDto> findProductStockByProductIdIn(List<Long> productIds);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.product.id in :productIds")
  List<Stock> findByProductIdInWithPessimisticLock(List<Long> productIds);

  List<Stock> findByIdIn(List<Long> ids);

//  @QueryHints(value = { @QueryHint(name = "org.hibernate.cacheable", value = "false") }) // 2차 캐시만 해당하나?
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.id in :ids")
  List<Stock> findByInIdsWithPessimisticLock(List<Long> ids);
}
