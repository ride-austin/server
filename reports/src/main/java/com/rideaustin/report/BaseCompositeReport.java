package com.rideaustin.report;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import com.google.common.collect.Sets;
import com.rideaustin.report.entry.CompositeReportEntry;

public abstract class BaseCompositeReport<P> extends BaseReport<CompositeReportEntry, P> implements CompositeReport {

  private final Set<BaseReport<?, P>> reports;

  public BaseCompositeReport(BaseReport<?, P>... reports) {
    this.reports = Sets.newHashSet(reports);
  }

  @Override
  protected void doExecute() {
    reports.forEach(BaseReport::doExecute);
  }

  public Set<Report<?, P>> getReports() {
    return Collections.unmodifiableSet(reports);
  }

  @Override
  public void setParameters(String parameters, Class<P> parameterClass) throws IOException {
    super.setParameters(parameters, parameterClass);
    for (Report<?, P> report : getReports()) {
      report.setParameters(parameters, parameterClass);
    }
  }
}
