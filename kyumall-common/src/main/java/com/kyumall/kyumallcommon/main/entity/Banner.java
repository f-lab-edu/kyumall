package com.kyumall.kyumallcommon.main.entity;

import com.kyumall.kyumallcommon.BaseTimeEntity;
import com.kyumall.kyumallcommon.upload.entity.Image;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor @NoArgsConstructor @Builder
@Entity
public class Banner extends BaseTimeEntity {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  @JoinColumn
  @ManyToOne(fetch = FetchType.LAZY)
  private BannerGroup bannerGroup;
  private String name;
  private String url;
  private Integer sortOrder;

  //@ManyToOne(fetch = FetchType.LAZY)
  //@JoinColumn
  //private Image image;
  private String imageName;
}
