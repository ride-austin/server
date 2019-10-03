package com.rideaustin.report;

import java.io.IOException;
import java.util.stream.Stream;

import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.rest.exception.ServerError;

public interface Report<T, P> {
  void execute() throws ServerError;
  Stream<T> getResultsStream();
  void setParameters(String parameters, Class<P> parameterClass) throws IOException;
  ReportMetadata getMetadata();
  void setMetadata(ReportMetadata reportMetadata);
  ReportAdapter<T> getAdapter();
  default boolean isComposite() {
    return this instanceof CompositeReport;
  }
}