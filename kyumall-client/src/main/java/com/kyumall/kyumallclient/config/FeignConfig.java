package com.kyumall.kyumallclient.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@EnableFeignClients("com.kyumall")
@Configuration
public class FeignConfig {

}
