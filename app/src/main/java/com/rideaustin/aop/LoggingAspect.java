package com.rideaustin.aop;

import javax.inject.Inject;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.rideaustin.service.CurrentUserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
@Order(100)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LoggingAspect {

  private static final int MAX_PARAM_LENGTH = 2048;

  private final CurrentUserService currentUserService;

  @Before("execution(* com.rideaustin.rest..*(..)) && !execution(* com.rideaustin.rest.Rest.healthCheck(..))")
  public void logBefore(JoinPoint joinPoint) {
    if (log.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder("Entering ");
      try {
        sb.append(getMethodAndUserInfo(joinPoint)).append(' ');
        sb.append(getParameters(joinPoint));
        log.debug(sb.toString());
      } catch (Exception th) {
        log.trace("Before: {%s}", th);
      }
    }
  }

  @After("execution(* com.rideaustin.rest..*(..)) && !execution(* com.rideaustin.rest.Rest.healthCheck(..))")
  public void logAfter(JoinPoint joinPoint) {
    if (log.isDebugEnabled()) {
      StringBuilder sb = new StringBuilder("Exited ");
      try {
        sb.append(getMethodAndUserInfo(joinPoint));
        log.debug(sb.toString());
      } catch (Exception th) {
        log.trace("After: {%s}", th);
      }
    }
  }

  private String getParameters(JoinPoint joinPoint) {
    StringBuilder sb = new StringBuilder();
    try {
      Object[] params = joinPoint.getArgs();
      for (int i = 0; i < params.length; i++) {
        if (params[i] != null) {
          sb.append("Parameter").append(i + 1).append(" = ").append(toString(params[i])).append(", ");
        }
      }
    } catch (Exception th) {
      log.trace("Parameters: {%s}", th);
    }
    return sb.toString();
  }

  /**
   * This method to avoid logging the full text for images encoded in base64 format
   *
   * @param object
   * @return
   */
  private Object toString(Object object) {
    String value = object.toString();
    if (value.length() > MAX_PARAM_LENGTH) {
      //Print first 80 chars only
      value = "<" + value.substring(0, 80) + ".....>";
    }
    return value;
  }

  private String getMethodAndUserInfo(JoinPoint joinPoint) {
    StringBuilder sb = new StringBuilder();
    try {
      sb.append(joinPoint.getTarget().getClass().getSimpleName()).append(" -> ");
      sb.append(joinPoint.getSignature().getName());
      //Make sure we don't fail due to null user
      if (currentUserService != null && currentUserService.getUser() != null) {
        sb.append(" [").append("user_id: ").append(currentUserService.getUser().getId());
        sb.append(", email: ").append(currentUserService.getUser().getEmail()).append("] ");
      }
    } catch (Exception th) {
      log.trace("Method and user info: {%s}", th);
    }
    return sb.toString();
  }
}
