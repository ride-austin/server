package com.rideaustin.report.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Set;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.rideaustin.report.params.ReportParameterProvider;

@Component
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface ReportComponent {

  long id();

  String name() ;

  String description() default "";

  ReportFormat format() default ReportFormat.CSV;

  String header();

  Param[] parameters() default {};

  Class<? extends ReportParameterProvider> parametersProvider() default NullProvider.class;

  boolean archive() default false;

  boolean upload() default false;

  @Retention(RetentionPolicy.RUNTIME)
  @interface Param {
    String label();

    String name();

    String description() default "";

    ReportParameterType type() default ReportParameterType.STRING;

    boolean required() default false;

    boolean internal() default false;

    String defaultValue() default "";

    int order();

    boolean list() default false;

    Class<? extends Enum> enumClass() default Enum.class;
  }

  class NullProvider implements ReportParameterProvider {
    @Override
    public Set<? extends ReportParameter> createParams() {
      return Collections.emptySet();
    }
  }
}
