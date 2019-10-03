package com.rideaustin.dispatch.aop;

import static com.rideaustin.dispatch.LogUtil.flowInfo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.statemachine.StateContext;
import org.springframework.web.context.request.async.DeferredResult;

import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
public class ActionAspect {

  @Around("execution(@com.rideaustin.dispatch.aop.DeferredResultAction * com.rideaustin.dispatch.actions..*(..))")
  public Object errorHandlingAspect(ProceedingJoinPoint joinPoint) throws Throwable {
    StateContext<States, Events> context = (StateContext<States, Events>) joinPoint.getArgs()[0];
    DeferredResult<ResponseEntity<Object>> contextResult = (DeferredResult<ResponseEntity<Object>>) context.getMessageHeader("result");
    try {
      Object result = joinPoint.proceed();
      if (contextResult != null) {
        flowInfo(log, StateMachineUtils.getRideId(context), "Setting deferred result");
        contextResult.setResult(ResponseEntity.ok().build());
      }
      return result;
    } catch (Throwable e) {
      if (contextResult != null) {
        flowInfo(log, StateMachineUtils.getRideId(context), "Setting deferred error result");
        contextResult.setErrorResult(e);
      }
      throw e;
    }
  }
}
