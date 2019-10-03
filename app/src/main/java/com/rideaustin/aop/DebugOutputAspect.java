package com.rideaustin.aop;

import javax.inject.Inject;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Profile("dev")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DebugOutputAspect {

  private final ObjectMapper objectMapper;

  @Around("execution(* com.rideaustin.rest..*(..)) && @annotation(com.rideaustin.aop.DebugOutput)")
  public Object project(ProceedingJoinPoint pjp) throws Throwable {
    Object ret = pjp.proceed();
    if (ret != null) {
      log.info(objectMapper.writeValueAsString(ret));
    }
    return ret;
  }
}
