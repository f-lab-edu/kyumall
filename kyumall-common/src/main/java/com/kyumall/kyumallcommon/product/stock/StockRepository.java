package com.kyumall.kyumallcommon.product.stock;

import com.kyumall.kyumallcommon.product.product.Product;
import com.kyumall.kyumallcommon.product.stock.Stock;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;

public interface StockRepository extends JpaRepository<Stock, Long> {
  Optional<Stock> findByProduct(Product product);

  @Query("select s from Stock s where s.product.id in :productIds")
  List<Stock> findByProductIdIn(List<Long> productIds);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.product.id in :productIds")
  List<Stock> findByProductIdInWithPessimisticLock(List<Long> productIds);

  List<Stock> findByIdIn(List<Long> ids);

  //@QueryHints(value = { @QueryHint(name = "org.hibernate.cacheable", value = "false")})
  @QueryHints(value = { @QueryHint(name = "org.hibernate.cacheable", value = "false") })
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s from Stock s where s.id in :ids")
  List<Stock> findByInIdsWithPessimisticLock(List<Long> ids);
}
