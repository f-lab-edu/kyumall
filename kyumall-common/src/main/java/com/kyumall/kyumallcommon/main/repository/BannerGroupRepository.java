package com.kyumall.kyumallcommon.main.repository;

import com.kyumall.kyumallcommon.main.entity.BannerGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BannerGroupRepository extends JpaRepository<BannerGroup, Long> {
  @EntityGraph(attributePaths = {"banners", "banners.image"})
  Optional<BannerGroup> findByName(String groupName);
}
