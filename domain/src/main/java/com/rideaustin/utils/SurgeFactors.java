package com.rideaustin.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

@Constraint(validatedBy = SurgeFactorsValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface SurgeFactors {
  String message() default "Surge factor must be assigned to existing car type";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
