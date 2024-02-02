package com.kyumall.kyumallclient;

import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 초기 데이터를 등록하는데 사용됩니다
 */
@Component
@RequiredArgsConstructor @Profile("default")
public class DataLoader implements CommandLineRunner {

  private final TermRepository termRepository;
  private final CategoryRepository categoryRepository;

  @Override
  public void run(String... args) throws Exception {
    saveTerms();
    saveCategories();
  }

  private void saveCategories() {
    Category food = Category.builder()
        .name("식품")
        .status(CategoryStatus.INUSE)
        .build();
    categoryRepository.save(food);
    Category fruit = Category.builder()
        .name("과일")
        .parent(food)
        .status(CategoryStatus.INUSE)
        .build();
    categoryRepository.save(fruit);
  }

  private void saveTerms() {
    Term kyumallTerm = Term.builder()
        .name("큐몰 이용약관")
        .content(
            "[ 큐몰 이용 약관 ] 제1장 총칙 제 1 조 (목적) 이 약관은 큐몰 주식회사(이하 “회사”)가 운영하는 사이버몰에서 제공하는 서비스와 이를 이용하는 회원의 권리·의무 및 책임사항을 규정함을 목적으로 합니다. 제 2 조 (용어의 정의) 이 약관에서 사용하는 용어의 정의는 다음과 같습니다. 그리고 여기에서 정의되지 않은 이 약관상의 용어의 의미는 일반적인 거래관행에 따릅니다. 1. “사이버몰”이란 회사가 상품 또는 용역 등(일정한 시설을 이용하거나 용역을 제공받을 수 있는 권리를 포함하며, 이하 “상품 등”)을 회원에게 제공하기 위하여 컴퓨터 등 정보통신설비를 이용하여 상품 등을 거래할 수 있도록 설정한 가상의 영업장 등 회사가 운영하는 웹사이트 및 모바일 웹, 앱 등을 모두 포함)을 의미합니다.")
        .type(TermType.REQUIRED)
        .status(TermStatus.INUSE).build();

    Term privateInfoTerm = Term.builder()
        .name("개인정보 수집 및 이용 동의")
        .content("개인정 수집 및 이용에 동의합니다.")
        .type(TermType.REQUIRED)
        .status(TermStatus.INUSE).build();

    Term marketingTerm = Term.builder()
        .name("마케팅 목적의 개인정보 수집 및 이용 동의 (선택)")
        .content("마케팅 목적으로 개인정보를 수집하고 이용하는 것에 동의합니다.")
        .type(TermType.OPTIONAL)
        .status(TermStatus.INUSE).build();

    termRepository.save(kyumallTerm);
    termRepository.save(privateInfoTerm);
    termRepository.save(marketingTerm);
  }
}
