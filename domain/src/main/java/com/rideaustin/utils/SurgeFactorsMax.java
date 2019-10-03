package com.rideaustin.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = SurgeFactorsMaxValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SurgeFactorsMax {
  String message() default "Surge factor may not exceed {value}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String value();
}
