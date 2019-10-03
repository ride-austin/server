package com.rideaustin.rest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Payload;

import org.springframework.http.HttpStatus;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface WhenInvalidReturn {

  HttpStatus value() default HttpStatus.OK;

  @WhenInvalidReturn(HttpStatus.UNPROCESSABLE_ENTITY)
  class HTTP422 implements Payload {}

}
