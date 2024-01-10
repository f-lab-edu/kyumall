package com.kyumall.kyumallcommon.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaAuditing
@EntityScan("com.kyumall.kyumallcommon")
@EnableJpaRepositories("com.kyumall.kyumallcommon")
public class JpaConfig {
}
