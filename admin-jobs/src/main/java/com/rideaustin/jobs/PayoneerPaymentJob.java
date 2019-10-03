package com.rideaustin.jobs;

import java.time.LocalDate;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.model.City;
import com.rideaustin.rest.model.PeriodicReportType;
import com.rideaustin.service.CityService;
import com.rideaustin.service.email.CSVEmailAttachment;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.ReportEmail;
import com.rideaustin.service.payment.EarningsReportingService;

@Component
public class PayoneerPaymentJob extends BaseJob {

  private EarningsReportingService earningsReportingService;
  private EmailService emailService;
  private CityService cityService;

  @Value("${jobs.payoneer_report.recipients}")
  private String recipients;

  private PeriodicReportType type = PeriodicReportType.WEEKLY;
  private LocalDate reportDate = LocalDate.now();

  @Override
  protected String getDescription() {
    return "Payoneer payment CSV export";
  }

  @Override
  protected void executeInternal() throws JobExecutionException {
    try {
      for (City city : cityService.findAll()) {
        String paymentCsv = earningsReportingService.getPaymentCsv(reportDate, type, city);
        String subject = "Earnings report for " + city.getName() + ", " + type.getPeriodDescription()
          + Constants.DATE_FORMATTER.format(reportDate.with(type.getStartAdjuster()));
        emailService.sendEmail(new ReportEmail(new CSVEmailAttachment(subject, paymentCsv), recipients, city));
      }
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }

  @Inject
  public void setEarningsReportingService(EarningsReportingService earningsReportingService) {
    this.earningsReportingService = earningsReportingService;
  }

  @Inject
  public void setEmailService(EmailService emailService) {
    this.emailService = emailService;
  }

  @Inject
  public void setCityService(CityService cityService) {
    this.cityService = cityService;
  }

  public void setReportDate(LocalDate reportDate) {
    this.reportDate = reportDate;
  }

  public void setType(PeriodicReportType type) {
    this.type = type;
  }

  public void setRecipients(String recipients) {
    this.recipients = recipients;
  }
}
