package com.kyumall.kyumallclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan("com.kyumall.kyumallcommon")
@SpringBootApplication(scanBasePackages = {"com.kyumall"})
public class KyumallClientApplication {

  public static void main(String[] args) {
    SpringApplication.run(KyumallClientApplication.class, args);
  }

}
