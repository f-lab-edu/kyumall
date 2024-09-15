package com.kyumall.kyumallcommon.product.product.repository;

import com.kyumall.kyumallcommon.product.product.entity.ProductImage;
import com.kyumall.kyumallcommon.product.product.entity.ProductImageKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, ProductImageKey> {

}
