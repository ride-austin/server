package com.rideaustin.service;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class GlobalExceptionHandlerTestConfig {

  @Bean
  GlobalExceptionHandlerTestingController globalExceptionHandlerTestingController() {
    return new GlobalExceptionHandlerTestingController();
  }

}
