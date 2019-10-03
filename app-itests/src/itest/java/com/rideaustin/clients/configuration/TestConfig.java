package com.rideaustin.clients.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Configuration
class TestConfig {

  @Bean
  ConfigurationItemsAdminAction configurationItemsAdminAction(WebApplicationContext context) {
    return new ConfigurationItemsAdminAction(webAppContextSetup(context).apply(springSecurity()).build());
  }

  @Bean
  ConfigurationItemsDriverAction configurationItemsDriverAction(WebApplicationContext context) {
    return new ConfigurationItemsDriverAction(webAppContextSetup(context).apply(springSecurity()).build());
  }

  @Bean
  ConfigurationItemChangedEventInterceptor configurationItemChangedEventInterceptor() {
    return new ConfigurationItemChangedEventInterceptor();
  }

}
