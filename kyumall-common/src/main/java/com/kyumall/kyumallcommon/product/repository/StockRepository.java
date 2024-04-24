package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.entity.Stock;
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

  List<Stock> findByIdIn(List<Long> ids);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select s "
      + "from Stock s "
      + "join fetch s.product "
      + "where s.id in :ids")
  List<Stock> findByInIdsWithPessimisticLock(List<Long> ids);
}
