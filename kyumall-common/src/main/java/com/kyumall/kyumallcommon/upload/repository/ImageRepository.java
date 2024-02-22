package com.kyumall.kyumallcommon.upload.repository;

import com.kyumall.kyumallcommon.upload.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
