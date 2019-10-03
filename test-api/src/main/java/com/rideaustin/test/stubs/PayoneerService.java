package com.rideaustin.test.stubs;

import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@Profile("itest")
public class PayoneerService extends com.rideaustin.service.thirdparty.PayoneerService {
  public PayoneerService(Environment env) {
    super(env);
  }

  @Override
  public String getSignupURL(String payoneerId) {
    return String.format("https://payoneer.com/signup?id=%s", payoneerId);
  }
}
