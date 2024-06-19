package com.kyumall.kyumallcommon.product.product;

import com.kyumall.kyumallcommon.exception.ErrorCode;
import com.kyumall.kyumallcommon.exception.KyumallException;
import com.kyumall.kyumallcommon.product.product.Product;
import com.kyumall.kyumallcommon.product.stock.Stock;
import com.kyumall.kyumallcommon.product.product.ProductRepository;
import com.kyumall.kyumallcommon.product.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class StockService {
  private final ProductRepository productRepository;
  private final StockRepository stockRepository;

  @Transactional
  public void updateStock(Long productId, Long memberId, Long quantity) {
    //TODO: 유효성 체크 필요
    Product product = findProduct(productId);
    Stock stock = findByProductOrCreateStock(product);
    stock.updateQuantity(quantity);
  }

  private Stock findByProductOrCreateStock(Product product) {
    return stockRepository.findByProduct(product)
        .orElseGet(() -> stockRepository.save(Stock.builder()
            .product(product)
            .quantity(0L)
            .build()));
  }

  private Product findProduct(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new KyumallException(ErrorCode.PRODUCT_NOT_EXISTS));
  }
}
