package com.rideaustin.test.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import com.rideaustin.config.CacheConfiguration;

@Profile("itest")
@Configuration
@Import(CacheConfiguration.class)
@ComponentScan("com.rideaustin.test.setup")
public class TestSetupConfig {


  @Profile("itest")
  @Bean
  public Long noAvailableDriverExpiration() {
    return 1L;
  }
}
