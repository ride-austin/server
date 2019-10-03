package com.rideaustin.report.params;

import java.time.Instant;
import java.util.List;

import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.rest.model.InspectionStickerStatus;
import com.rideaustin.rest.model.ListDriversParams;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class DriversExportReportParams extends AvatarExportReportParams {

  private Long driverId;
  private List<PayoneerStatus> payoneerStatus;
  private List<String> carCategory;
  private List<CityApprovalStatus> cityApprovalStatus;
  private List<DriverActivationStatus> activationStatus;
  private List<CarInspectionStatus> carInspectionStatus;
  private List<DocumentStatus> driverLicenseStatus;
  private List<DocumentStatus> insuranceStatus;
  private List<DocumentStatus> carPhotosStatus;
  private List<DocumentStatus> profilePhotosStatus;
  private List<InspectionStickerStatus> inspectionStickerStatus;
  private List<DriverOnboardingStatus> onboardingStatus;
  private Instant signedUpAfter;
  private Instant signedUpBefore;
  private Instant createdOnAfter;
  private Instant createdOnBefore;
  private Long cityId;
  private Long lastCommunicationLaterThan;
  private Long onboardingPendingLongerThan;

  public DriversExportReportParams(ListDriversParams listDriversParams) {
    super(listDriversParams.getName(), listDriversParams.getEmail(), listDriversParams.getActive(), listDriversParams.getEnabled());
    this.driverId = listDriversParams.getDriverId();
    this.payoneerStatus = listDriversParams.getPayoneerStatus();
    this.carCategory = listDriversParams.getCarCategory();
    this.cityApprovalStatus = listDriversParams.getCityApprovalStatus();
    this.activationStatus = listDriversParams.getActivationStatus();
    this.carInspectionStatus = listDriversParams.getCarInspectionStatus();
    this.onboardingStatus = listDriversParams.getOnboardingStatus();
    this.signedUpAfter = listDriversParams.getSignedUpAfter();
    this.signedUpBefore = listDriversParams.getSignedUpBefore();
    this.createdOnAfter = listDriversParams.getCreatedOnAfter();
    this.createdOnBefore = listDriversParams.getCreatedOnBefore();
    this.cityId = listDriversParams.getCityId();
    this.lastCommunicationLaterThan = listDriversParams.getLastCommunicationLaterThan();
    this.onboardingPendingLongerThan = listDriversParams.getOnboardingPendingLongerThan();
  }

  public ListDriversParams asListDriversParams() {
    ListDriversParams listDriversParams = new ListDriversParams();
    listDriversParams.setName(getName());
    listDriversParams.setEmail(getEmail());
    listDriversParams.setActive(getActive());
    listDriversParams.setEnabled(getEnabled());
    listDriversParams.setDriverId(this.driverId);
    listDriversParams.setPayoneerStatus(this.payoneerStatus);
    listDriversParams.setCarCategory(this.carCategory);
    listDriversParams.setCityApprovalStatus(this.cityApprovalStatus);
    listDriversParams.setActivationStatus(this.activationStatus);
    listDriversParams.setCarInspectionStatus(this.carInspectionStatus);
    listDriversParams.setOnboardingStatus(this.onboardingStatus);
    listDriversParams.setSignedUpAfter(this.signedUpAfter);
    listDriversParams.setSignedUpBefore(this.signedUpBefore);
    listDriversParams.setCreatedOnAfter(this.createdOnAfter);
    listDriversParams.setCreatedOnBefore(this.createdOnBefore);
    listDriversParams.setCityId(this.cityId);
    listDriversParams.setLastCommunicationLaterThan(this.lastCommunicationLaterThan);
    listDriversParams.setOnboardingPendingLongerThan(this.onboardingPendingLongerThan);
    return listDriversParams;
  }

}