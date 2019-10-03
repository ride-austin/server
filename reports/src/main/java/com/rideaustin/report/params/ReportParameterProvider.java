package com.rideaustin.report.params;

import java.util.Set;

import com.rideaustin.report.model.ReportParameter;

@FunctionalInterface
public interface ReportParameterProvider {
  Set<? extends ReportParameter> createParams();
}
