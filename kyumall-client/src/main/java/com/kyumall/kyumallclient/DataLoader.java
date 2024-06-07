package com.kyumall.kyumallclient;

import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.entity.Term;
import com.kyumall.kyumallcommon.member.entity.TermDetail;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.repository.TermDetailRepository;
import com.kyumall.kyumallcommon.member.repository.TermRepository;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import com.kyumall.kyumallcommon.member.vo.TermStatus;
import com.kyumall.kyumallcommon.member.vo.TermType;
import com.kyumall.kyumallcommon.product.entity.Category;
import com.kyumall.kyumallcommon.product.repository.CategoryRepository;
import com.kyumall.kyumallcommon.product.vo.CategoryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 초기 데이터를 등록하는데 사용됩니다
 */
@Component
@RequiredArgsConstructor @Profile("local")
public class DataLoader implements CommandLineRunner {

  private final MemberRepository memberRepository;
  private final PasswordService passwordService;
  private final TermRepository termRepository;
  private final TermDetailRepository termDetailRepository;
  private final CategoryRepository categoryRepository;
  private final CacheManager cacheManager;

  @Override
  public void run(String... args) throws Exception {
    saveMember();
    saveTerms();
    saveCategories();
    System.out.println("cacheManager is " + this.cacheManager.getClass().getName());
  }

  private void saveMember() {
    memberRepository.saveAndFlush(Member.builder()
        .username("test01")
        .email("test01@email.com")
        .password(passwordService.encrypt("1234"))
        .type(MemberType.CLIENT)
        .status(MemberStatus.INUSE)
        .build());
  }

  private void saveCategories() {
    Category food = saveCategory("식품", null);
    Category meet = saveCategory("육류", food);
    Category fruit = saveCategory("과일", food);
    Category apple = saveCategory("사과", fruit);
    Category banana = saveCategory("바나나", fruit);
  }

  private Category saveCategory(String name, Category parent) {
    return categoryRepository.save(Category.builder()
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build());
  }

  private void saveTerms() {
    Term kyumallTerm = termRepository.save(Term.builder()
        .name("큐몰 이용약관")
        .ordering(1)
        .type(TermType.REQUIRED)
        .status(TermStatus.INUSE).build());

    Term marketingTerm = termRepository.save(Term.builder()
        .name("마케팅 동의 약관")
        .ordering(1)
        .type(TermType.OPTIONAL)
        .status(TermStatus.INUSE).build());

    TermDetail kyumallTermDetail = TermDetail.builder()
        .term(kyumallTerm)
        .title("큐몰 이용 약관 (필수)")
        .content(
            "[ 큐몰 이용 약관 ] 제1장 총칙 제 1 조 (목적) 이 약관은 큐몰 주식회사(이하 “회사”)가 운영하는 사이버몰에서 제공하는 서비스와 이를 이용하는 회원의 권리·의무 및 책임사항을 규정함을 목적으로 합니다. 제 2 조 (용어의 정의) 이 약관에서 사용하는 용어의 정의는 다음과 같습니다. 그리고 여기에서 정의되지 않은 이 약관상의 용어의 의미는 일반적인 거래관행에 따릅니다. 1. “사이버몰”이란 회사가 상품 또는 용역 등(일정한 시설을 이용하거나 용역을 제공받을 수 있는 권리를 포함하며, 이하 “상품 등”)을 회원에게 제공하기 위하여 컴퓨터 등 정보통신설비를 이용하여 상품 등을 거래할 수 있도록 설정한 가상의 영업장 등 회사가 운영하는 웹사이트 및 모바일 웹, 앱 등을 모두 포함)을 의미합니다.")
        .version(1)
        .build();

    TermDetail marketingTermDetail = TermDetail.builder()
        .term(marketingTerm)
        .title("마케팅 목적의 개인정보 수집 및 이용 동의 (선택) ")
        .content("마케팅 목적으로 개인정보를 수집하고 이용하는 것에 동의합니다.")
        .version(1)
        .build();
    termDetailRepository.save(kyumallTermDetail);
    termDetailRepository.save(marketingTermDetail);
  }
}
