package com.kyumall.kyumallcommon.upload.repository;

import com.kyumall.kyumallcommon.upload.entity.TempImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TempImageRepository extends JpaRepository<TempImage, Long> {
  List<TempImage> findByIdIn(List<Long> ids);
}
