package com.rideaustin.service.reports;

import java.util.Map;

import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.rest.exception.BadRequestException;

@FunctionalInterface
public interface ReportParametersValidator {

  <P> void validate(Map<String, ReportParameter> parameterMapping, P parameters) throws BadRequestException;
}
