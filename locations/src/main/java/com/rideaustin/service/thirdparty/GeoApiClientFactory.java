package com.rideaustin.service.thirdparty;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class GeoApiClientFactory implements BeanFactoryAware {

  private BeanFactory beanFactory;
  private final Environment environment;

  public GeoApiClient createGeoApiClient() {
    Provider provider = getProvider();
    switch (provider) {
      case GOOGLE:
        return beanFactory.getBean(GeoApiClientGoogleImpl.class);
      case MOCK:
      default:
        return beanFactory.getBean(GeoApiClientMockImpl.class);
    }
  }

  private Provider getProvider() {
    return Provider.from(environment.getProperty("map.api.default.provider", "google"));
  }

  enum Provider {
    GOOGLE,
    MOCK;

    public static Provider from(String value) {
      return valueOf(value.toUpperCase());
    }
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

}
