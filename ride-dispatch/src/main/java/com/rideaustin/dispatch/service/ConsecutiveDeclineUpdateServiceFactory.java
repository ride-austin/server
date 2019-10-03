package com.rideaustin.dispatch.service;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import com.rideaustin.dispatch.service.queue.QueueConsecutiveDeclineUpdateService;
import com.rideaustin.service.model.context.DispatchType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ConsecutiveDeclineUpdateServiceFactory {

  private final BeanFactory beanFactory;

  public ConsecutiveDeclineUpdateService createService(DispatchType type) {
    if (type == DispatchType.QUEUED) {
      return beanFactory.getBean(QueueConsecutiveDeclineUpdateService.class);
    }
    return beanFactory.getBean("consecutiveDeclineUpdateService", DefaultConsecutiveDeclineUpdateService.class);
  }
}
