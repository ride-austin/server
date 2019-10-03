package com.rideaustin.config;

import org.springframework.context.annotation.Bean;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;

@org.springframework.context.annotation.Configuration
public class FreemarkerConfig {
  private freemarker.template.Configuration cfg;
  
  @Bean
  public Configuration configuration() {
    if (cfg == null) {
      cfg = new Configuration(Configuration.VERSION_2_3_24);
      cfg.setClassForTemplateLoading(FreemarkerConfig.class, "/templates");
      cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      cfg.setLogTemplateExceptions(false);
      cfg.setDefaultEncoding("UTF-8");
    }
    
    return cfg;
  }
}
