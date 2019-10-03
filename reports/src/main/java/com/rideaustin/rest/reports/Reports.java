package com.rideaustin.rest.reports;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.report.model.ReportMetadata;
import com.rideaustin.report.model.ReportParameter;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ListReportParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.reports.ReportService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/reports")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Reports {

  private final ReportService reportService;

  @GetMapping
  @WebClientEndpoint
  @ApiOperation(value = "Get list of available reports", response = ReportMetadata.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK")
  })
  public Page<ReportMetadata> listReports(
    @ApiParam @ModelAttribute ListReportParams searchCriteria,
    @ApiParam @ModelAttribute PagingParams paging
  ) {
    return reportService.listReports(searchCriteria, paging);
  }

  @WebClientEndpoint
  @GetMapping("/{reportId}")
  @ApiOperation("Get report metadata")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Report not found")
  })
  public ReportMetadata getReport(
    @ApiParam(value = "Report ID", example = "1") @PathVariable Long reportId
  ) throws NotFoundException {
    return reportService.getReport(reportId);
  }

  @WebClientEndpoint
  @GetMapping("/{reportId}/parameters")
  @ApiOperation("Get list of report parameters")
  public List<ReportParameter> listParameters(
    @ApiParam(value = "Report ID", example = "1") @PathVariable Long reportId
  ) {
    return reportService.listParameters(reportId);
  }

  @WebClientEndpoint
  @ResponseStatus(value = HttpStatus.ACCEPTED)
  @ApiOperation("Trigger report execution job")
  @PostMapping(value = "/{reportId}/execute", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_ACCEPTED, message = "Job is triggered"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to trigger job")
  })
  public void executeReport(
    @ApiParam(value = "Report ID", example = "1") @PathVariable Long reportId,
    @ApiParam("Report parameters, json-formatted") @RequestBody(required = false) String parametersJson
  ) throws RideAustinException {
    reportService.executeReport(reportId, parametersJson);
  }

}