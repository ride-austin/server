package com.rideaustin.jobs.export;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.rideaustin.jobs.GenericReportJob;
import com.rideaustin.jobs.JobExecutionException;
import com.rideaustin.report.DriversExportReport;
import com.rideaustin.report.params.DriversExportReportParams;
import com.rideaustin.rest.model.ListDriversParams;

import lombok.Setter;

@Component
public class DriversExportJob extends GenericReportJob {

  @Setter(onMethod = @__(@Inject))
  private Environment environment;

  @Setter
  private ListDriversParams params;

  public DriversExportJob() {
    this.reportClass = DriversExportReport.class;
  }

  @Override
  protected String getDescription() {
    return "drivers CSV export";
  }

  @Override
  protected void executeInternal() throws JobExecutionException {
    if (recipients == null) {
      recipients = Lists.newArrayList(environment.getProperty("jobs.drivers_report.recipients").split(","));
    }
    try {
      if (paramsJson == null && params == null) {
        paramsJson = GenericReportJob.EMPTY_PARAMETERS_JSON;
      } else if (params != null) {
        paramsJson = objectMapper.writeValueAsString(new DriversExportReportParams(params));
      }
      super.executeInternal();
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
