package com.rideaustin.config;

import javax.annotation.Nonnull;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.GeoApiContext;
import com.rideaustin.repo.dsl.CityRestrictionDslRepository;
import com.rideaustin.security.RAAuthenticationProvider;
import com.rideaustin.security.UserDetailsServiceImpl;
import com.rideaustin.service.CityService;
import com.rideaustin.service.city.CityValidationService;
import com.rideaustin.service.city.DefaultCityValidationService;
import com.rideaustin.service.city.StubCityValidationService;
import com.rideaustin.utils.CryptUtils;
import com.twilio.http.TwilioRestClient;

@Configuration
@ComponentScan(
  basePackageClasses = com.rideaustin.Constants.class,
  excludeFilters = @Filter(value = {RestController.class, ControllerAdvice.class, EnableWebMvc.class})
)
@EnableAsync
@EnableCaching
@EnableAspectJAutoProxy(proxyTargetClass = true)
@PropertySource("classpath:${spring.profiles.active:dev}.properties")
public class AppConfig {

  @Bean
  @Nonnull
  public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
    return new PropertySourcesPlaceholderConfigurer();
  }

  @Bean
  public AmazonSNSClient amazonSNSClient(Environment env) {
    return new AmazonSNSClient(new BasicAWSCredentials(env.getProperty("ra.aws.key.access"),
      env.getProperty("ra.aws.key.secret")));
  }

  @Bean
  public TwilioRestClient twilioRestClient(Environment env) {
    return new TwilioRestClient.Builder(env.getProperty("sms.twilio.sid"), env.getProperty("sms.twilio.token")).build();
  }

  @Bean
  @Profile("dev")
  public CityValidationService stubCityValidationService(CityService cityService, CityRestrictionDslRepository restrictionRepository,
    ObjectMapper mapper) {
    return new StubCityValidationService(cityService, restrictionRepository, mapper);
  }

  @Bean
  @Profile("!dev")
  public CityValidationService cityValidationService(CityService cityService, CityRestrictionDslRepository restrictionRepository,
    ObjectMapper objectMapper) {
    return new DefaultCityValidationService(cityService, restrictionRepository, objectMapper);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationProvider hashingAuthenticationProvider(UserDetailsServiceImpl userDetailsService, CryptUtils cryptUtils) {
    RAAuthenticationProvider daoAuthenticationProvider =
      new RAAuthenticationProvider(cryptUtils);
    daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    daoAuthenticationProvider.setUserDetailsService(userDetailsService);
    return daoAuthenticationProvider;
  }

  @Bean
  public GeoApiContext geoApiContext(Environment env) {
    return new GeoApiContext().setApiKey(env.getProperty("google.maps.key"));
  }

}
