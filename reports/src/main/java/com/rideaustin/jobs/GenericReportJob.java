package com.rideaustin.jobs;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.City;
import com.rideaustin.report.Report;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.params.NullReportParams;
import com.rideaustin.report.render.ReportRenderer;
import com.rideaustin.service.CityService;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.email.ReportEmail;
import com.rideaustin.service.reports.ReportExecutionService;
import com.rideaustin.service.reports.ReportService;

import lombok.Setter;

@Component
public class GenericReportJob extends BaseJob {

  protected static final String EMPTY_PARAMETERS_JSON = "{}";

  @Setter(onMethod = @__(@Inject))
  protected ObjectMapper objectMapper;

  @Setter(onMethod = @__(@Inject))
  private ReportService reportService;

  @Setter(onMethod = @__(@Inject))
  private ReportExecutionService reportExecutionService;

  @Setter(onMethod = @__(@Inject))
  private BeanFactory beanFactory;

  @Setter(onMethod = @__(@Inject))
  private EmailService emailService;

  @Setter(onMethod = @__(@Inject))
  private CityService cityService;

  @Setter
  private Long reportId;
  @Setter
  protected String paramsJson;
  @Setter
  protected List<String> recipients;
  @Setter
  protected Class<?> reportClass;

  @Override
  protected void executeInternal() throws JobExecutionException {
    ReportMetadata reportMetadata;
    try {
      if (reportClass == null && reportId != null) {
        reportMetadata = reportService.getReport(reportId);
        reportClass = reportMetadata.getReportClass();
      } else {
        reportMetadata = reportService.getReportForClass(reportClass);
      }
      if (reportClass == null) {
        return;
      }
      String reportClassName = reportClass.getName();
      String reportParamsClassName;
      if (reportMetadata.getParameters().isEmpty()) {
        reportParamsClassName = NullReportParams.class.getName();
      } else {
        reportParamsClassName = resolveParamsClassName(reportClassName, reportClass);
      }

      Report report = (Report) beanFactory.getBean(reportClass);
      report.setParameters(paramsJson, Class.forName(reportParamsClassName));
      report.setMetadata(reportMetadata);

      // generate report
      ReportRenderer renderer = reportExecutionService.execute(report);

      // email report
      City city = determineCity();
      final ReportEmail reportEmail = renderer.createEmailFor(report, recipients, city);
      if (reportEmail.hasAttachments()) {
        emailService.sendEmail(reportEmail);
      }
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }

  protected String resolveParamsClassName(String reportClassName, Class<?> reportClass) {
    return reportClassName.replace(reportClass.getSimpleName(),
      String.format("params.%sParams", reportClass.getSimpleName()));
  }

  @Override
  protected String getDescription() {
    return "generic report id: " + reportId;
  }

  /**
   * Determine the city:
   * - if report parameters contain 'cityId' - load city by id
   * - otherwise - use default city
   *
   * @return
   * @throws IOException
   */
  private City determineCity() throws IOException {
    Long cityId = null;

    if (this.paramsJson != null) {
      // if report params contain 'cityId' param - try to extract it
      cityId = Optional.ofNullable(objectMapper.readTree(paramsJson).findValue("cityId"))
        .map(JsonNode::asLong).orElse(null);
    }

    return cityService.getById(cityId);  // returns default city when cityId param is null
  }
}
