package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  @EntityGraph(attributePaths = {"parent"})
  List<Category> findAllByStatus(CategoryStatus status);
}
