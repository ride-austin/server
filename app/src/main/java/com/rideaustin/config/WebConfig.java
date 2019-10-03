package com.rideaustin.config;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.joda.money.Money;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.SpringDataWebConfiguration;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Controller;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.enums.AvatarType;

import lombok.RequiredArgsConstructor;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.BasicAuth;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.DocExpansion;
import springfox.documentation.swagger.web.ModelRendering;
import springfox.documentation.swagger.web.OperationsSorter;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;
import springfox.documentation.swagger.web.TagsSorter;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableRetry
@EnableWebMvc
@Configuration
@EnableSwagger2
@ComponentScan(
  basePackageClasses = com.rideaustin.Constants.class,
  includeFilters = @Filter({Controller.class, RestController.class, ControllerAdvice.class}),
  useDefaultFilters = false)
@EnableAspectJAutoProxy
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class WebConfig extends SpringDataWebConfiguration {

  private final ObjectMapper objectMapper;
  private final Environment env;

  @Override
  public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    configurer.enable();
  }

  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    configurer.favorParameter(false);
    configurer.favorPathExtension(false);
  }

  @Bean
  public MultipartResolver multipartResolver() {
    CommonsMultipartResolver resolver = new CommonsMultipartResolver();
    long maxUploadSize = env.getProperty("file.upload.max.size", Long.class, 250L * 1024L * 1024L);
    resolver.setMaxUploadSize(maxUploadSize);
    return resolver;
  }

  @Bean
  public ViewResolver viewResolver() {
    InternalResourceViewResolver resolver = new InternalResourceViewResolver();
    resolver.setViewClass(InternalResourceView.class);
    resolver.setPrefix("/WEB-INF/static/");
    resolver.setSuffix(".html");
    return resolver;
  }

  @Bean
  public MethodValidationPostProcessor methodValidationPostProcessor() {
    return new MethodValidationPostProcessor();
  }

  @Override
  public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    setupJson(converters);
    setupText(converters);
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    super.addFormatters(registry);
    registry.addConverter(new AvatarTypeAliasResolvingConverter());
  }

  private void setupText(List<HttpMessageConverter<?>> converters) {
    StringHttpMessageConverter string2Converter = new StringHttpMessageConverter();
    string2Converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_PLAIN));
    converters.add(string2Converter);
  }

  private void setupJson(List<HttpMessageConverter<?>> converters) {
    MappingJackson2HttpMessageConverter jackson2Converter = new MappingJackson2HttpMessageConverter(objectMapper);
    jackson2Converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
    converters.add(jackson2Converter);
  }

  @Bean
  public Docket swaggerSpringMvcPlugin() {
    return new Docket(DocumentationType.SWAGGER_2)
      .directModelSubstitute(Money.class, BigDecimal.class)
      .genericModelSubstitutes(ResponseEntity.class)
      .useDefaultResponseMessages(false)
      .enableUrlTemplating(true)
      .securitySchemes(Collections.singletonList(securityScheme()))
      .securityContexts(Collections.singletonList(securityContext()));
  }

  private SecurityContext securityContext() {
    return SecurityContext.builder()
      .securityReferences(Collections.singletonList(new SecurityReference("basic", new AuthorizationScope[]{new AuthorizationScope("scope", "scope")})))
      .forPaths(PathSelectors.any())
      .build();
  }

  private SecurityScheme securityScheme() {
    return new BasicAuth("basic");
  }

  @Bean
  public SecurityConfiguration swaggerSecurityConfiguration() {
    return SecurityConfigurationBuilder.builder()
      .useBasicAuthenticationWithAccessCodeGrant(true)
      .build();
  }

  @Bean
  UiConfiguration uiConfig() {
    return UiConfigurationBuilder.builder()
      .deepLinking(true)
      .displayOperationId(false)
      .defaultModelsExpandDepth(1)
      .defaultModelExpandDepth(1)
      .defaultModelRendering(ModelRendering.EXAMPLE)
      .displayRequestDuration(false)
      .docExpansion(DocExpansion.NONE)
      .filter(false)
      .maxDisplayedTags(null)
      .operationsSorter(OperationsSorter.ALPHA)
      .showExtensions(false)
      .tagsSorter(TagsSorter.ALPHA)
      .supportedSubmitMethods(UiConfiguration.Constants.DEFAULT_SUBMIT_METHODS)
      .validatorUrl(null)
      .build();
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    if (env.getProperty("swagger.enable", Boolean.class, false)) {
      registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
      registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }
    registry.addResourceHandler("/fonts/**").addResourceLocations("/WEB-INF/static/fonts/");
  }

  public static class AvatarTypeAliasResolvingConverter implements Converter<String, AvatarType> {

    @Override
    public AvatarType convert(String source) {
      return AvatarType.valueOf(source).resolveAlias();
    }
  }

}
