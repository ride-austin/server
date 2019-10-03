package com.rideaustin.report;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Stream;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.reports.ReportParametersDefaultValueProvider;
import com.rideaustin.service.reports.ReportParametersValidator;

public abstract class BaseReport<T, P> implements Report<T, P> {

  private ObjectMapper mapper;
  private ReportParametersValidator parametersValidator;
  private ReportParametersDefaultValueProvider defaultValuesProvider;

  protected P parameters;
  protected Stream<T> resultsStream;
  protected ReportMetadata metadata;
  protected ReportAdapter<T> adapter;

  @Override
  public void setParameters(String parameters, Class<P> parameterClass) throws IOException {
    this.parameters = mapper.readValue(parameters, parameterClass);
  }

  @Override
  public Stream<T> getResultsStream() {
    return resultsStream;
  }

  @Override
  public void setMetadata(ReportMetadata metadata) {
    this.metadata = metadata;
  }

  @Override
  public ReportMetadata getMetadata() {
    return metadata;
  }

  public ReportAdapter<T> getAdapter() {
    if (adapter == null) {
      adapter = createAdapter();
    }
    return adapter;
  }

  protected abstract ReportAdapter<T> createAdapter();

  @Override
  public void execute() throws ServerError {
    try {
      Map<String, ReportParameter> parameterMapping = createParameterMapping();
      defaultValuesProvider.fillDefaultValues(parameterMapping, parameters);
      parametersValidator.validate(parameterMapping, parameters);
      doExecute();
    } catch (BadRequestException e) {
      throw new ServerError(e);
    }
  }

  protected abstract void doExecute();

  private Map<String, ReportParameter> createParameterMapping() {
    return metadata.getParameters().stream().collect(toMap(ReportParameter::getParameterName, identity()));
  }

  @Inject
  public void setMapper(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  @Inject
  public void setParametersValidator(ReportParametersValidator parametersValidator) {
    this.parametersValidator = parametersValidator;
  }

  @Inject
  public void setDefaultValuesProvider(ReportParametersDefaultValueProvider defaultValuesProvider) {
    this.defaultValuesProvider = defaultValuesProvider;
  }
}
