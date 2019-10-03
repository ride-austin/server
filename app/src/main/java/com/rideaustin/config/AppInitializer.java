package com.rideaustin.config;

import javax.servlet.Filter;
import javax.servlet.ServletRegistration;

import org.springframework.core.annotation.Order;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import com.rideaustin.application.cache.impl.RedisConfiguration;
import com.rideaustin.dispatch.config.RideFlowConfig;
import com.rideaustin.filter.ClientAppVersionFilter;

@Order(0)
public class AppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class[]{AppConfig.class, DataSourceConfig.class, JpaConfig.class, RedisConfiguration.class, RideFlowConfig.class, CacheConfiguration.class};
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[]{WebConfig.class, SecurityConfig.class};
  }

  @Override
  protected String[] getServletMappings() {
    return new String[]{"/"};
  }

  @Override
  protected Filter[] getServletFilters() {
    CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
    encodingFilter.setEncoding("UTF-8");
    encodingFilter.setForceEncoding(true);

    ClientAppVersionFilter avcf = new ClientAppVersionFilter();

    return new Filter[]{encodingFilter, avcf};
  }

  @Override
  protected void customizeRegistration(ServletRegistration.Dynamic registration) {
    registration.setInitParameter("dispatchOptionsRequest", "true");
  }

}
