package com.rideaustin.service.reports;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.DefaultDateValues;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ReportParametersDefaultValueProviderImpl implements ReportParametersDefaultValueProvider {

  private static final EnumSet<DefaultDateValues> MACROS = EnumSet.allOf(DefaultDateValues.class);

  @Override
  public <P> void fillDefaultValues(Map<String, ReportParameter> parameterMapping, P parameters) {
    for (Map.Entry<String, ReportParameter> parameterInfo : parameterMapping.entrySet()) {
      try {
        String fieldName = parameterInfo.getKey();
        Object value = PropertyUtils.getProperty(parameters, fieldName);
        ReportParameter parameter = parameterInfo.getValue();
        ReportParameterType parameterType = parameter.getParameterType();

        String defaultValue = parameter.getDefaultValue();
        if (value == null && StringUtils.isNotEmpty(defaultValue)) {
          if (EnumSet.of(ReportParameterType.DATE, ReportParameterType.DATETIME).contains(parameterType)) {
            DefaultDateValues macro = DefaultDateValues.valueOf(defaultValue);
            if (MACROS.contains(macro)) {
              PropertyUtils.setProperty(parameters, fieldName, macro.getValue());
            }
          } else {
            PropertyUtils.setProperty(parameters, fieldName, parameterType.defaultValueConverter().apply(parameter));
          }
        }
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        log.error("Error setting property", e);
      }
    }
  }
}
