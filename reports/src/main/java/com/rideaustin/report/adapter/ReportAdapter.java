package com.rideaustin.report.adapter;

import java.util.Map;
import java.util.function.Function;

public interface ReportAdapter<T> {

  Function<T, String[]> getRowMapper();

  Map<String, Object> getReportContext();

  String[] getHeaders();
}
