package com.kyumall.kyumallclient.product;

import com.kyumall.kyumallclient.exception.ErrorCode;
import com.kyumall.kyumallclient.exception.KyumallException;
import com.kyumall.kyumallclient.product.dto.CreateProductRequest;
import com.kyumall.kyumallclient.product.dto.ProductSimpleDto;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.entity.Product;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ProductService {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;

  public Long createProduct(CreateProductRequest request) {
    Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new KyumallException(ErrorCode.CATEGORY_NOT_EXISTS));

    Product product = productRepository.save(Product.builder()
        .name(request.getProductName())
        .category(category)
        .price(request.getPrice())
        .detail(request.getDetail())
        .build());
    return product.getId();
  }

  public Page<ProductSimpleDto> getAllProducts(Pageable pageable) {
    return productRepository.findAllByOrderByName(pageable).map(ProductSimpleDto::from);
  }
}
