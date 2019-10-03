package com.rideaustin.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = SurgeFactorsMinValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SurgeFactorsMin {
  String message() default "Surge factor may not be less than {value}";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String value();
}
