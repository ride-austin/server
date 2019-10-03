package com.rideaustin.rest;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.querydsl.core.Tuple;
import com.rideaustin.Constants;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.ZipCodeReportAssembler;
import com.rideaustin.jobs.GenericReportJob;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.report.RidesExportReport;
import com.rideaustin.report.params.RidesExportReportParams;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.ListRidesParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.RideReportService;
import com.rideaustin.service.SchedulerService;
import com.rideaustin.service.model.CumulativeRidesReportEntry;
import com.rideaustin.service.model.DriverRidesReportEntry;
import com.rideaustin.service.model.RideReportEntry;
import com.rideaustin.service.model.ZipCodeReportEntry;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideReports {

  private final RideReportService rideService;
  private final ZipCodeReportAssembler zipCodeReportAssembler;
  private final CurrentUserService currentUserService;
  private final ObjectMapper mapper;
  private final SchedulerService schedulerService;

  @GetMapping("/rest/reports/ridesReport")
  @ApiOperation("Get ride stats report (total rides, mileage, cancellations, etc)")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public List<RideReportEntry> riderReport(
    @ApiParam(value = "Report start date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnAfter,
    @ApiParam(value = "Report end date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnBefore,
    @ApiParam("Zip code") @RequestParam(required = false) String zipCode,
    @ApiParam("Timezone offset") @RequestParam(required = false) String timeZoneOffset,
    @ApiParam(value = "City ID", example = "1") @RequestParam(required = false) Long cityId) {
    return rideService.getRidesReport(completedOnAfter, completedOnBefore, zipCode,
      Optional.ofNullable(cityId).orElse(Constants.DEFAULT_CITY_ID), timeZoneOffset);
  }

  @GetMapping("/rest/reports/driversRidesReport")
  @ApiOperation(value = "Get report on ride fulfillment per driver", response = DriverRidesReportEntry.class, responseContainer = "List")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public Page<DriverRidesReportEntry> driversRidesReport(
    @ApiParam(value = "Report start date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnAfter,
    @ApiParam(value = "Report end date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnBefore,
    @ApiParam("Zip code") @RequestParam(required = false) String zipCode,
    @ApiParam(value = "City ID", example = "1") @RequestParam(required = false) Long cityId,
    @ApiParam PagingParams pagingParams
  ) {
    return rideService.getRidesByUsersReport(completedOnAfter, completedOnBefore, zipCode, cityId, pagingParams);
  }

  @WebClientEndpoint
  @GetMapping("/rest/reports/cumulativeRidesReport")
  @ApiOperation("Get cumulative rides report, combining rides stats and ride fulfillment per driver")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public CumulativeRidesReportEntry cumulativeRidesReport(
    @ApiParam(value = "Report start date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnAfter,
    @ApiParam(value = "Report end date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnBefore,
    @ApiParam("Zip code") @RequestParam(required = false) String zipCode,
    @ApiParam(value = "City ID", example = "1") @RequestParam(required = false) Long cityId,
    @ApiParam PagingParams pagingParams
  ) {
    return rideService.getCumulativeRidesReport(completedOnAfter, completedOnBefore, zipCode, cityId, pagingParams);
  }

  @WebClientEndpoint
  @GetMapping("/rest/reports/ridesZipCodeReport")
  @ApiOperation(value = "Get rides report per zip code", responseContainer = "List", response = ZipCodeReportEntry.class)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public Iterable<ZipCodeReportEntry> ridesZipCodeReport(
    @ApiParam(value = "Report start date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnAfter,
    @ApiParam(value = "Report end date", required = true, example = "2000-10-31T01:30:00.000-05:00") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant completedOnBefore,
    @ApiParam("Zip code") @RequestParam(required = false) String zipCode,
    @ApiParam PagingParams pagingParams
  ) {
    Page<Tuple> zipCodeResult = rideService.getRidesZipCodeReport(completedOnAfter, completedOnBefore, zipCode, pagingParams);
    return zipCodeResult.map(zipCodeReportAssembler);
  }

  @PostMapping("/rest/rides/report")
  @ApiOperation("Trigger extended rides report")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Server is either misconfigured, or failed to trigger report job")
  })
  public void ridesReport(
    @ApiParam @ModelAttribute ListRidesParams params,
    @ApiParam("Statuses to include in report") @RequestParam(required = false) List<RideStatus> status,
    @ApiParam("List of emails to receive the report") @RequestParam(required = false) List<String> recipient
  ) throws ServerError {

    if (status == null || status.isEmpty()) {
      params.setStatus(Collections.singletonList(RideStatus.COMPLETED));
    } else {
      params.setStatus(status);
    }

    List<String> resolvedRecipients = recipient;
    if (CollectionUtils.isEmpty(resolvedRecipients)) {
      resolvedRecipients = Collections.singletonList(currentUserService.getUser().getEmail());
    }
    try {
      exportRides(params, resolvedRecipients);
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  @WebClientEndpoint
  @PostMapping("/rest/rides/export")
  @ApiOperation("Trigger extended rides report. Same as POST /rest/rides/report, include all the statuses, email to current user")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Server is either misconfigured, or failed to trigger report job")
  })
  public void exportRides(
    @ApiParam @ModelAttribute ListRidesParams params
  ) throws ServerError {
    try {
      exportRides(params, Collections.singletonList(currentUserService.getUser().getEmail()));
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  private void exportRides(ListRidesParams params, List<String> recipient) throws SchedulerException, ServerError {
    Map<String, Object> dataMap;
    try {
      dataMap = ImmutableMap.of(
        "paramsJson", mapper.writeValueAsString(new RidesExportReportParams(params)),
        "recipients", recipient,
        "reportClass", RidesExportReport.class
      );
    } catch (JsonProcessingException e) {
      throw new ServerError(e);
    }
    schedulerService.triggerJob(GenericReportJob.class, dataMap);
  }

}
