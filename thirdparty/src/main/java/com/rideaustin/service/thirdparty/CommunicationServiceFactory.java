package com.rideaustin.service.thirdparty;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rideaustin.model.thirdparty.CallbackResponse;
import com.rideaustin.service.thirdparty.CommunicationService.Provider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CommunicationServiceFactory {

  private final BeanFactory beanFactory;
  private final Environment environment;

  public CommunicationService createCommunicationService() {
    Provider provider = getProvider();
    switch (provider) {
      case TWILIO:
        return beanFactory.getBean(TwilioService.class);
      case AMAZON:
      default:
        return beanFactory.getBean(AmazonSNSService.class);
    }
  }

  public CommunicationCallbackService createCallbackService() {
    Provider provider = getProvider();
    if (Provider.TWILIO.equals(provider)) {
      return beanFactory.getBean(TwilioCallbackService.class);
    }
    return new DefaultCommunicationCallbackService();
  }

  private Provider getProvider() {
    return Provider.from(environment.getProperty("sms.default.provider", "twilio"));
  }

  private static class DefaultCommunicationCallbackService implements CommunicationCallbackService {
    @Override
    public CallbackResponse createCallbackResponse(String callSid, String from) {
      return null;
    }

    @Override
    public CallbackResponse createSmsCallbackResponse(String messageSid, String from, String body) {
      return null;
    }
  }
}
