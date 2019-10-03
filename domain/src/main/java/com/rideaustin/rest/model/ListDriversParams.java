package com.rideaustin.rest.model;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.rideaustin.Constants;
import com.rideaustin.model.QDriverEmailHistoryItem;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.QDriver;
import com.rideaustin.service.user.CarTypesUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel
public class ListDriversParams extends ListAvatarsParams {

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private static final QDriver qDriver = QDriver.driver;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private static final QCar qCar = QCar.car;
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private static final QDriverEmailHistoryItem qDriverCommunication = QDriverEmailHistoryItem.driverEmailHistoryItem;

  @Getter
  @Setter
  @ApiModelProperty(name = "Driver ID", example = "1")
  private Long driverId;
  @Getter
  @Setter
  @ApiModelProperty(name = "Payoneer status", dataType = "List", allowableValues = "INITIAL,PENDING,ACTIVE", notes = "Initial = \"not registered\", PENDING = \"inactive\". These statuses can be used interchangeably")
  private List<PayoneerStatus> payoneerStatus;
  @Getter
  @Setter
  @ApiModelProperty(name = "Car category", dataType = "List", notes = "Please refer to car categories list")
  private List<String> carCategory;
  @Getter
  @Setter
  @ApiModelProperty(name = "City approval status", dataType = "List", allowableValues = "PENDING,NOT_PROVIDED,APPROVED,REJECTED_PHOTO,REJECTED_BY_CITY,EXPIRED")
  private List<CityApprovalStatus> cityApprovalStatus;
  @Getter
  @Setter
  @ApiModelProperty(name = "Driver activation status", dataType = "List", allowableValues = "ACTIVE,REJECTED,SUSPENDED,DEACTIVATED_OTHER,INACTIVE")
  private List<DriverActivationStatus> activationStatus;
  @Getter
  @Setter
  @ApiModelProperty(name = "Car inspection status", dataType = "List", allowableValues = "APPROVED,REJECTED,PENDING,NOT_INSPECTED")
  private List<CarInspectionStatus> carInspectionStatus;
  @Getter
  @Setter
  @ApiModelProperty(name = "Last communication later than X days", example = "1")
  private Long lastCommunicationLaterThan;
  @Getter
  @Setter
  @ApiModelProperty(name = "Driver is in PENDING onboarding status more than X days", example = "1")
  private Long onboardingPendingLongerThan;
  @Getter
  @Setter
  @ApiModelProperty(name = "Onboarding status", dataType = "List", allowableValues = "REJECTED,SUSPENDED,PENDING,FINAL_REVIEW,ACTIVE")
  private List<DriverOnboardingStatus> onboardingStatus;
  @Getter
  @Setter
  @ApiModelProperty(name = "Signed up after", dataType = "Instant", example = "2016-01-01T00:00:00.000Z")
  private Instant signedUpAfter;
  @Getter
  @Setter
  @ApiModelProperty(name = "Signed up before", dataType = "Instant", example = "2016-01-01T00:00:00.000Z")
  private Instant signedUpBefore;
  @Getter
  @Setter
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @ApiModelProperty(name = "Created after", dataType = "Instant", example = "2016-01-01T00:00:00.000Z")
  private Instant createdOnAfter;
  @Getter
  @Setter
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @ApiModelProperty(name = "Created before", dataType = "Instant", example = "2016-01-01T00:00:00.000Z")
  private Instant createdOnBefore;
  @Getter
  @Setter
  @ApiModelProperty(name = "City ID", example = "1")
  private Long cityId;

  public void fill(BooleanBuilder builder) {
    fill(builder, qDriver.user);
    if (driverId != null) {
      builder.and(qDriver.id.eq(driverId));
    }
    if (getActive() != null) {
      builder.and(qDriver.active.eq(getActive()));
    }
    if (payoneerStatus != null) {
      builder.and(qDriver.payoneerStatus.in(payoneerStatus));
    }
    if (createdOnAfter != null) {
      builder.and(qDriver.createdDate.goe(Date.from(createdOnAfter)));
    }
    if (createdOnBefore != null) {
      builder.and(qDriver.createdDate.loe(Date.from(createdOnBefore)));
    }
    if (carCategory != null) {
      BooleanBuilder carCategoryBuilder = new BooleanBuilder();
      for (String category : carCategory) {
        int bitmask = CarTypesUtils.getCarType(category).getBitmask();
        carCategoryBuilder.or(qCar.carCategoriesBitmask.mod(bitmask * 2)
          .divide(bitmask).floor().eq(1));
      }
      carCategoryBuilder.and(qCar.removed.isFalse());
      JPQLQuery<Driver> carSubQuery = JPAExpressions.select(qCar.driver).from(qCar).where(carCategoryBuilder);
      builder.and(qDriver.in(carSubQuery));
    }
    if (cityApprovalStatus != null) {
      builder.and(qDriver.cityApprovalStatus.in(cityApprovalStatus));
    }
    if (carInspectionStatus != null) {
      JPQLQuery<Driver> carInspectionStatusQuery = JPAExpressions.select(qCar.driver).from(qCar)
        .where(qCar.inspectionStatus.in(carInspectionStatus));
      builder.and(qDriver.in(carInspectionStatusQuery));
    }
    if (onboardingStatus != null) {
      builder.and(qDriver.onboardingStatus.in(onboardingStatus));
    }
    if (signedUpAfter != null) {
      builder.and(qDriver.agreementDate.goe(Date.from(signedUpAfter)));
    }
    if (signedUpBefore != null) {
      builder.and(qDriver.agreementDate.loe(Date.from(signedUpBefore)));
    }
    if (activationStatus != null) {
      builder.and(qDriver.activationStatus.in(activationStatus));
    }
    if (cityId != null) {
      builder.and(qDriver.cityId.eq(cityId));
    }
    if (lastCommunicationLaterThan != null) {
      Date from = Date.from(Instant.now().minusSeconds(Constants.SECONDS_PER_DAY.longValue() * lastCommunicationLaterThan));
      JPQLQuery<Long> commSubQuery = JPAExpressions.select(qDriverCommunication.driverId).from(qDriverCommunication)
        .where(qDriverCommunication.createdDate.before(from));
      builder.and(qDriver.id.in(commSubQuery));
    }
    if (onboardingPendingLongerThan != null) {
      Date from = Date.from(Instant.now().minusSeconds(Constants.SECONDS_PER_DAY.longValue() * onboardingPendingLongerThan));
      builder.and(qDriver.onboardingPendingSince.before(from));
    }
  }


}
