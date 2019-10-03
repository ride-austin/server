package com.rideaustin.service.thirdparty;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public abstract class StripeCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return getProvider(context).contains(getValue());
  }

  protected String getProvider(ConditionContext context) {
    return context.getEnvironment().getProperty("stripe.api.default.provider");
  }

  protected abstract String getValue();

}
