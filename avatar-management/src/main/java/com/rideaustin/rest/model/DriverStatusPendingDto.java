package com.rideaustin.rest.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class DriverStatusPendingDto {
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with payoneer status PENDING, INACTIVE")
  private final long payoneer;
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with driver license status PENDING")
  private final long driverLicense;
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with insurance status PENDING")
  private final long insurance;
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with city approval status PENDING, NOT_PROVIDED")
  private final long cityApproval;
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with car inspection status PENDING, NOT_INSPECTED")
  private final long carInspection;
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with car photos status PENDING")
  private final long carPhotos;
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with profile photos status PENDING")
  private final long profilePhotos;
  @ApiModelProperty(dataType = "Long", notes = "Count of drivers with inspection sticker status PENDING")
  private final long inspectionSticker;

  private DriverStatusPendingDto(long payoneer, long driverLicense, long insurance,
    long cityApproval, long carInspection, long carPhotos, long profilePhotos, long inspectionSticker) {
    this.payoneer = payoneer;
    this.driverLicense = driverLicense;
    this.insurance = insurance;
    this.cityApproval = cityApproval;
    this.carInspection = carInspection;
    this.carPhotos = carPhotos;
    this.profilePhotos = profilePhotos;
    this.inspectionSticker = inspectionSticker;
  }

  public static class Builder {

    private long payoneer;
    private long driverLicense;
    private long insurance;
    private long cityApproval;
    private long carInspection;
    private long carPhotos;
    private long profilePhotos;
    private long inspectionSticker;

    public Builder payoneer(long payoneer) {
      this.payoneer = payoneer;
      return this;
    }

    public Builder driverLicense(long driverLicense) {
      this.driverLicense = driverLicense;
      return this;
    }

    public Builder insurance(long insurance) {
      this.insurance = insurance;
      return this;
    }

    public Builder cityApproval(long cityApproval) {
      this.cityApproval = cityApproval;
      return this;
    }

    public Builder carInspection(long carInspection) {
      this.carInspection = carInspection;
      return this;
    }

    public Builder carPhotos(long carPhotos) {
      this.carPhotos = carPhotos;
      return this;
    }

    public Builder profilePhotos(long profilePhotos) {
      this.profilePhotos = profilePhotos;
      return this;
    }

    public Builder inspectionSticker(long inspectionSticker) {
      this.inspectionSticker = inspectionSticker;
      return this;
    }

    public DriverStatusPendingDto build() {
      return new DriverStatusPendingDto(payoneer, driverLicense, insurance, cityApproval,
        carInspection, carPhotos, profilePhotos, inspectionSticker);
    }
  }

  public long getPayoneer() {
    return payoneer;
  }

  public long getDriverLicense() {
    return driverLicense;
  }

  public long getInsurance() {
    return insurance;
  }

  public long getCityApproval() {
    return cityApproval;
  }

  public long getCarInspection() {
    return carInspection;
  }

  public long getCarPhotos() {
    return carPhotos;
  }

  public long getProfilePhotos() {
    return profilePhotos;
  }

  public long getInspectionSticker() {
    return inspectionSticker;
  }
}
