package com.rideaustin.service.surgepricing;

import javax.inject.Inject;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SurgeRecalculationServiceFactory implements BeanFactoryAware {

  private BeanFactory beanFactory;
  private final SurgeRecalculationConfigProvider configProvider;

  public SurgeRecalculationService createRecalculationService(Long cityId) {
    SurgeRecalculationConfig config = configProvider.getConfig(cityId);
    return beanFactory.getBean(config.getSurgeProvider().getImplClass());
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    this.beanFactory = beanFactory;
  }
}
