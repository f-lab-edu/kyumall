package com.kyumall.kyumallcommon.product.product.repository;

import com.kyumall.kyumallcommon.product.product.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductQuerydslRepository {
  List<Product> search();

  Optional<Product> searchById(Long id);
}
