package com.kyumall.kyumallcommon.product.category;

import com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto;
import feign.Param;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, Long> {

  @EntityGraph(attributePaths = {"parent"})
  List<Category> findAllByStatus(CategoryStatus status);

  @Query("""
SELECT new com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto(
 c.id, c.name, (SELECT COUNT(sc) > 0 FROM Category sc WHERE sc.parent.id = c.id))
FROM Category c WHERE c.parent.id is null
""")
  List<SubCategoryDto> findTopSubCategoriesUsingSubquery();

  @Query("""
SELECT new com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto(
 c.id, c.name, (SELECT COUNT(sc) > 0 FROM Category sc WHERE sc.parent.id = c.id))
FROM Category c WHERE c.parent.id = :parentId
""")
  List<SubCategoryDto> findSubCategoriesUsingSubquery(@Param("parentId") Long parentId);

  @Query("""
SELECT new com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto(
 c.id, c.name, COUNT(1))
FROM Category c 
  LEFT JOIN Category sc ON sc.parent.id = c.id
WHERE c.parent.id = :parentId
GROUP BY c.id, c.name
""")
  List<SubCategoryDto> findSubCategoriesUsingJoin(@Param("parentId") Long parentId);

  @Query("""
SELECT new com.kyumall.kyumallcommon.product.category.dto.SubCategoryDto(
 c.id, c.name, COUNT(1))
FROM Category c 
  LEFT JOIN Category sc ON sc.parent.id = c.id
WHERE c.parent.id is null
GROUP BY c.id, c.name
""")
  List<SubCategoryDto> findTopSubCategoriesUsingJoin();

  /**
   * 카테고리ID 로 서브 카테고리를 조회합니다.
   *
   * @deprecated 전체 카테고리를 캐시하도록 변경하여, 현재 사용하지 않습니다.
   * @param categoryId
   * @return
   */
  @Deprecated
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
  List<Long> findSubCategoryById(Long categoryId);
}
