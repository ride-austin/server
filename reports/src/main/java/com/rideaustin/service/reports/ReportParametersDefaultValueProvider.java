package com.rideaustin.service.reports;

import java.util.Map;

import com.rideaustin.report.model.ReportParameter;

@FunctionalInterface
public interface ReportParametersDefaultValueProvider {
  <P> void fillDefaultValues(Map<String, ReportParameter> parameterMapping, P parameters);
}
