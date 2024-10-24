package com.kyumall.kyumallclient;

import com.kyumall.kyumallclient.member.MemberService;
import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.mail.domain.EmailTemplate;
import com.kyumall.kyumallcommon.mail.repository.EmailTemplateRepository;
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
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.category.CategoryStatus;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

/**
 * 어플리케이션 초기 기동시 동작하는 코드
 */
@Slf4j
@Profile({"local", "dev"})
@Component
@RequiredArgsConstructor
public class AppInitialRunner implements CommandLineRunner {

  private final MemberRepository memberRepository;
  private final PasswordService passwordService;
  private final TermRepository termRepository;
  private final TermDetailRepository termDetailRepository;
  private final CategoryRepository categoryRepository;
  private final CacheManager cacheManager;
  private final EmailTemplateRepository emailTemplateRepository;
  private final ResourceLoader resourceLoader;
  @Value("${spring.mail.password}")
  private String mailPassword;

  @Override
  public void run(String... args) throws Exception {
//    saveMember();
//    saveTerms();
    //saveCategories();
//    saveCategoriesAutomatically(null, 0);
//    saveEmailTemplate();
    logJVMSettings();
    log.info("mail Password: {}", mailPassword);
    log.info("cacheManager is {}", this.cacheManager.getClass().getName());
  }

  private void logJVMSettings() {
    Runtime runtime = Runtime.getRuntime();

    long maxMemory = runtime.maxMemory(); // 최대 메모리 (Xmx)
    long totalMemory = runtime.totalMemory(); // 현재 할당된 총 메모리 (Xms 부근)
    long freeMemory = runtime.freeMemory(); // 사용 가능한 메모리
    long usedMemory = totalMemory - freeMemory; // 사용 중인 메모리

    log.info("Max Memory (Xmx): " + maxMemory / (1024 * 1024) + " MB");
    log.info("Total Memory (allocated): " + totalMemory / (1024 * 1024) + " MB");
    log.info("Free Memory (in allocated): " + freeMemory / (1024 * 1024) + " MB");
    log.info("Used Memory: " + usedMemory / (1024 * 1024) + " MB");
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

  // 쿠팡과 동일한 갯수로 카테고리 데이터를 생성
  private void saveCategoriesAutomatically(Category parent, int depth) {
    if (depth == 3) { // 3 뎁스 까지
      return;
    }
    int rep = (depth == 2) ? 10 : 15; // 1뎁스 15개, 2뎁스 15개, 3뎁스 10개
    for (int i = 0; i < rep; i++) {
      long parentId = parent == null ? 0 : parent.getId();
      Category newCategory = saveCategory("category " + parentId + " " + depth + " " + i, parent);
      saveCategoriesAutomatically(newCategory, depth+1);
    }
  }

  private void saveCategories() {

    Category food = saveCategory("식품", null);
    Category meet = saveCategory("육류", food);
    Category fruit = saveCategory("과일", food);
    Category apple = saveCategory("사과", fruit);
    Category banana = saveCategory("바나나", fruit);
  }

  private Category saveCategory(String name, Category parent) {
    return categoryRepository.saveAndFlush(Category.builder()
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

  private void saveEmailTemplate() {
    // 이메일 템플릿
    createEmailTemplate(MemberService.SIGNUP_VERIFICATION_EMAIL_TEMPLATE_ID, "Kyumall 계정 인증", "email-template/signup-verification-template");
    createEmailTemplate(MemberService.TEMPORARY_PASSWORD_EMAIL_TEMPLATE_ID, "Kyumall 계정 비밀번호 재설정", "email-template/temporary-password-template");
  }

  private EmailTemplate createEmailTemplate(String templateId, String subject, String fileName) {
    EmailTemplate emailTemplate = loadEmailTemplate(templateId, subject, fileName);
    return emailTemplateRepository.saveAndFlush(emailTemplate);
  }

  private EmailTemplate loadEmailTemplate(String templateId, String subject ,String fileName) {
    try {
      String readFile = getResourceAsString(fileName);
      return EmailTemplate.builder()
          .id(templateId)
          .subject(subject)
          .template(readFile)
          .build();
    } catch (IOException e) {
      throw new IllegalStateException("템플릿 로드 중 오류가 발생", e);
    }
  }

  private String getResourceAsString(String fileName) throws IOException {
    Resource resource = resourceLoader.getResource("classpath:" + fileName + ".html");
    try (InputStream inputStream = resource.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }
  }
}
