package com.rideaustin.report;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.City;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.repo.dsl.DriverReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.FingerprintStatusReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.FingerprintStatusReportParams;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.service.CityService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ReportComponent(
  id = 4,
  name = "Fingerprint Status Report",
  description = "Fingerprint Status Report",
  header = "Fingerprint Status Report for {city}",
  parameters = {
    @ReportComponent.Param(
      label = "City",
      name = "city",
      type = ReportParameterType.ENUM,
      enumClass = CityToReport.class,
      required = true,
      order = 1
    ),
    @ReportComponent.Param(
      label = "Fingerprint Clearance status",
      name = "fingerPrintCleared",
      type = ReportParameterType.ENUM,
      enumClass = CityApprovalStatus.class,
      order = 2
    )
  }
)
public class FingerprintStatusReport extends BaseReport<FingerprintStatusReportEntry, FingerprintStatusReportParams> {

  private DriverReportDslRepository driverReportDslRepository;
  private CityService cityService;

  @Inject
  public FingerprintStatusReport(DriverReportDslRepository driverReportDslRepository, CityService cityService) {
    this.driverReportDslRepository = driverReportDslRepository;
    this.cityService = cityService;
  }

  @Override
  protected ReportAdapter<FingerprintStatusReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(FingerprintStatusReportEntry.class,
      ImmutableMap.of("city", parameters.getCity().getName()));
  }

  @Override
  protected void doExecute() {
    Long[] cities = new Long[0];
    try {
      if (parameters.getCity() == CityToReport.ALL) {
        cities = cityService.findAll().stream().map(City::getId).toArray(Long[]::new);
      } else {
        cities = new Long[]{cityService.getByName(parameters.getCity().name()).getId()};
      }
    } catch (NotFoundException e) {
      log.error("Error occurred", e);
    }
    this.resultsStream = driverReportDslRepository
      .findDriversWithFingerprintCleared(parameters.getFingerPrintCleared(), cities)
      .stream();
  }
}
