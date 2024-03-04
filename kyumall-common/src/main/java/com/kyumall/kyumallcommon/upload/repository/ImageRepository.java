package com.kyumall.kyumallcommon.upload.repository;

import com.kyumall.kyumallcommon.upload.entity.Image;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
  Optional<Image> findByStoredFileName(String storedFileName);
}
