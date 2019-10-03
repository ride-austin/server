package com.rideaustin.report.adapter;

import java.util.Map;

import com.rideaustin.report.entry.PromocodeRedemptionsCountByDateReportEntry;

public class PromocodeRedemptionsCountReportAdapter<T extends PromocodeRedemptionsCountByDateReportEntry> extends DefaultReportAdapter<T> {
  public PromocodeRedemptionsCountReportAdapter(Class<T> entryClass, Map<String, Object> context) {
    super(entryClass, context);
  }
}
