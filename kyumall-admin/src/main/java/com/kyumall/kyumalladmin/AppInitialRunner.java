package com.kyumall.kyumalladmin;

import com.kyumall.kyumallcommon.auth.authentication.passwword.PasswordService;
import com.kyumall.kyumallcommon.member.entity.Member;
import com.kyumall.kyumallcommon.member.repository.MemberRepository;
import com.kyumall.kyumallcommon.member.vo.MemberStatus;
import com.kyumall.kyumallcommon.member.vo.MemberType;
import com.kyumall.kyumallcommon.product.category.Category;
import com.kyumall.kyumallcommon.product.category.CategoryRepository;
import com.kyumall.kyumallcommon.product.category.CategoryStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * 어플리케이션 초기 기동시 동작하는 코드
 */
@Slf4j
@Profile({"dev", "local"})
@Component
@RequiredArgsConstructor
public class AppInitialRunner implements CommandLineRunner {

  private final MemberRepository memberRepository;
  private final PasswordService passwordService;
  private final CategoryRepository categoryRepository;
  private final CacheManager cacheManager;

  @Override
  public void run(String... args) throws Exception {
    saveMember();
    saveCategories();
    logJVMSettings();
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

  private void saveCategories() {
    saveCategory("식품", null);
  }

  private Category saveCategory(String name, Category parent) {
    return categoryRepository.saveAndFlush(Category.builder()
        .name(name)
        .parent(parent)
        .status(CategoryStatus.INUSE)
        .build());
  }
}
