package com.example.kyumallbatch;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class KyumallBatchApplication {

  public static void main(String[] args) {
    //SpringApplication.run(KyumallBatchApplication.class, args);
    new SpringApplicationBuilder(KyumallBatchApplication.class)
        .web(WebApplicationType.NONE) // 웹 환경 비활성화
        .run(args);
  }
}
