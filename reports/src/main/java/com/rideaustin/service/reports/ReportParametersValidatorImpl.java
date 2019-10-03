package com.rideaustin.service.reports;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Component;

import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.rest.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportParametersValidatorImpl implements ReportParametersValidator {

  @Override
  public <P> void validate(Map<String, ReportParameter> parameterMapping, P parameters) throws BadRequestException {
    try {
      for (Map.Entry<String, ReportParameter> parameterInfo : parameterMapping.entrySet()) {
        String name = parameterInfo.getKey();
        Object value = PropertyUtils.getProperty(parameters, name);
        ReportParameter parameter = parameterInfo.getValue();
        if (parameter.isRequired()) {
          if (value == null)
            throw new BadRequestException(String.format("Required parameter %s is null", name));
          if (ReportParameterType.STRING.equals(parameter.getParameterType()) && String.valueOf(value).isEmpty()) {
            throw new BadRequestException(String.format("Required string parameter %s is empty", name));
          }
        }
      }
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      log.error("Failed to read property", e);
      throw new BadRequestException("Failed to read parameter");
    }
  }
}
