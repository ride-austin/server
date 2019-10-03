package com.rideaustin.service.thirdparty.lookup.condition;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotatedTypeMetadata;

public abstract class LookupServiceCondition implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    return matches(context.getEnvironment());
  }

  private boolean matches(Environment environment){
    String configuredValue = environment.getProperty("lookup.api.default.provider");
    return StringUtils.contains(configuredValue, getValue());
  }

  protected abstract String getValue();
}
