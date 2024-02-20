package com.kyumall.kyumallcommon.product.repository;

import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  @EntityGraph(attributePaths = {"parent"})
  List<Category> findAllByStatus(CategoryStatus status);

  @Query(
      value = "WITH RECURSIVE cte AS ("
          + "  SELECT id, parent_id, name, 0 as depth from category c "
          + "  where parent_id = :categoryId "
          + "  UNION ALL "
          + "  SELECT c.id, c.parent_id, c.name, cte.depth + 1 as depth from cte "
          + "  join category c on cte.id = c.parent_id "
          + ") "
          + "SELECT id from cte "
  , nativeQuery = true)
  List<Long> findSubCategoryIds(Long categoryId);
}
