package com.kyumall.kyumallclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication(scanBasePackages = {"com.kyumall"})
public class KyumallClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(KyumallClientApplication.class, args);
  }
}
