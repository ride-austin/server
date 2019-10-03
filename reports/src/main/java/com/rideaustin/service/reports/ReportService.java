package com.rideaustin.service.reports;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.jobs.GenericReportJob;
import com.rideaustin.report.ReportsMetadataProvider;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListReportParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.SchedulerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ReportService {

  private static final String REPORTS = "reports";
  private final SchedulerService schedulerService;
  private final CurrentUserService currentUserService;
  private final ReportsMetadataProvider metadataProvider;

  public Page<ReportMetadata> listReports(ListReportParams searchCriteria, PagingParams paging) {
    return metadataProvider.findReports(searchCriteria, paging);
  }

  public ReportMetadata getReport(Long reportId) throws NotFoundException {
    return metadataProvider.findOne(reportId)
      .orElseThrow(() -> new NotFoundException("Report not found"));
  }

  public ReportMetadata getReportForClass(Class clazz) throws NotFoundException {
    return metadataProvider.findByClass(clazz)
      .orElseThrow(() -> new NotFoundException("Report not found"));
  }

  public List<ReportParameter> listParameters(Long reportId) {
    return metadataProvider.listParameters(reportId);
  }

  public void executeReport(Long reportId, String parametersJson) throws RideAustinException {
    Map<String, Object> jobParams = ImmutableMap.of(
      "reportId", reportId,
      "paramsJson", Optional.ofNullable(parametersJson).orElse("{}"),
      "recipients", currentUserService.getUser().getEmail()
    );
    String jobName = "report_" + reportId + UUID.randomUUID();
    schedulerService.triggerJob(GenericReportJob.class, jobName, REPORTS, jobParams);
  }
}
