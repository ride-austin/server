package com.rideaustin.report;

import java.util.Date;

import javax.inject.Inject;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.repo.dsl.DriverReportDslRepository;
import com.rideaustin.report.adapter.DefaultReportAdapter;
import com.rideaustin.report.adapter.ReportAdapter;
import com.rideaustin.report.entry.DriversExportReportEntry;
import com.rideaustin.report.model.ReportComponent;
import com.rideaustin.report.model.ReportParameterType;
import com.rideaustin.report.params.DriversExportReportParams;
import com.rideaustin.rest.model.InspectionStickerStatus;

@ReportComponent(
  id = 7,
  name = "Drivers Export report",
  description = "Drivers Export report",
  header = "Drivers export for {reportDate}",
  parameters = {
    @ReportComponent.Param(
      label = "Name",
      name = "name",
      type = ReportParameterType.STRING,
      order = 1
    ),
    @ReportComponent.Param(
      label = "Email",
      name = "email",
      type = ReportParameterType.STRING,
      order = 2
    ),
    @ReportComponent.Param(
      label = "Active",
      name = "active",
      type = ReportParameterType.BOOLEAN,
      order = 3
    ),
    @ReportComponent.Param(
      label = "Enabled",
      name = "enabled",
      type = ReportParameterType.BOOLEAN,
      order = 4
    ),
    @ReportComponent.Param(
      label = "Driver ID",
      name = "driverId",
      type = ReportParameterType.INTEGER,
      order = 5
    ),
    @ReportComponent.Param(
      label = "Payoneer status",
      name = "payoneerStatus",
      type = ReportParameterType.ENUM,
      enumClass = PayoneerStatus.class,
      order = 6
    ),
    @ReportComponent.Param(
      label = "City approval status",
      name = "cityApprovalStatus",
      type = ReportParameterType.ENUM,
      enumClass = CityApprovalStatus.class,
      order = 7
    ),
    @ReportComponent.Param(
      label = "Driver activation status",
      name = "activationStatus",
      type = ReportParameterType.ENUM,
      enumClass = DriverActivationStatus.class,
      order = 8
    ),
    @ReportComponent.Param(
      label = "Car inspection status",
      name = "carInspectionStatus",
      type = ReportParameterType.ENUM,
      enumClass = CarInspectionStatus.class,
      order = 9
    ),
    @ReportComponent.Param(
      label = "Driver license status",
      name = "driverLicenseStatus",
      type = ReportParameterType.ENUM,
      enumClass = DocumentStatus.class,
      order = 10
    ),
    @ReportComponent.Param(
      label = "Insurance status",
      name = "insuranceStatus",
      type = ReportParameterType.ENUM,
      enumClass = DocumentStatus.class,
      order = 11
    ),
    @ReportComponent.Param(
      label = "Car photos status",
      name = "carPhotosStatus",
      type = ReportParameterType.ENUM,
      enumClass = DocumentStatus.class,
      order = 12
    ),
    @ReportComponent.Param(
      label = "Profile photos status",
      name = "profilePhotosStatus",
      type = ReportParameterType.ENUM,
      enumClass = DocumentStatus.class,
      order = 13
    ),
    @ReportComponent.Param(
      label = "Inspection sticker status",
      name = "inspectionStickerStatus",
      type = ReportParameterType.ENUM,
      enumClass = InspectionStickerStatus.class,
      order = 14
    ),
    @ReportComponent.Param(
      label = "Onboarding status",
      name = "onboardingStatus",
      type = ReportParameterType.ENUM,
      enumClass = DriverOnboardingStatus.class,
      order = 15
    ),
    @ReportComponent.Param(
      label = "Car category",
      name = "carCategory",
      type = ReportParameterType.STRING,
      order = 16
    ),
    @ReportComponent.Param(
      label = "Created after",
      name = "createdOnAfter",
      type = ReportParameterType.DATETIME,
      order = 17
    ),
    @ReportComponent.Param(
      label = "Created before",
      name = "createdOnBefore",
      type = ReportParameterType.DATETIME,
      order = 18
    ),
    @ReportComponent.Param(
      label = "City ID",
      name = "cityId",
      type = ReportParameterType.INTEGER,
      order = 19
    )
  }
)
public class DriversExportReport extends BaseReport<DriversExportReportEntry, DriversExportReportParams> {

  private final DriverReportDslRepository driverReportDslRepository;

  @Inject
  public DriversExportReport(DriverReportDslRepository driverReportDslRepository) {
    this.driverReportDslRepository = driverReportDslRepository;
  }

  @Override
  protected ReportAdapter<DriversExportReportEntry> createAdapter() {
    return new DefaultReportAdapter<>(DriversExportReportEntry.class,
      ImmutableMap.of("reportDate", Date.from(DefaultDateValues.CURRENT_DATE.getValue())));
  }

  @Override
  protected void doExecute() {
    this.resultsStream = driverReportDslRepository.exportDrivers(parameters.asListDriversParams()).stream();
  }
}
