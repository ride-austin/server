package com.rideaustin.jobs.export;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.rideaustin.jobs.GenericReportJob;
import com.rideaustin.jobs.JobExecutionException;
import com.rideaustin.report.CustomPaymentsReport;
import com.rideaustin.report.params.CustomPaymentsReportParams;

import lombok.Setter;

@Component
public class CustomPaymentExportJob extends GenericReportJob {

  @Setter
  private Instant createAfter;
  @Setter
  private Instant createBefore;
  @Setter
  private Instant paymentDate;
  @Setter
  private Long cityId;

  public CustomPaymentExportJob() {
    this.reportClass = CustomPaymentsReport.class;
  }

  @Override
  protected String getDescription() {
    return "Custom payment CSV export";
  }

  @Override
  protected void executeInternal() throws JobExecutionException {
    try {
      if (paramsJson == null) {
        paramsJson = objectMapper.writeValueAsString(new CustomPaymentsReportParams(createAfter, createBefore, paymentDate, cityId));
      }
      super.executeInternal();
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }
}
