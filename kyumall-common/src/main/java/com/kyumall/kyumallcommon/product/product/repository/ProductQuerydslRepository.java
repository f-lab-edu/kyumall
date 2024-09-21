package com.kyumall.kyumallcommon.product.product.repository;

import com.kyumall.kyumallcommon.product.product.dto.ProductSearchDto;
import com.kyumall.kyumallcommon.product.product.dto.SearchProductCondition;
import com.kyumall.kyumallcommon.product.product.entity.Product;
import java.util.List;
import java.util.Optional;

public interface ProductQuerydslRepository {
  List<ProductSearchDto> search(SearchProductCondition request);

  Optional<Product> searchById(Long id);
}
