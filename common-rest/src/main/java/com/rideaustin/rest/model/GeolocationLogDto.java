package com.rideaustin.rest.model;

import java.util.Date;

import com.rideaustin.model.GeolocationLog;
import com.rideaustin.model.enums.GeolocationLogEvent;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class GeolocationLogDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final Date createdDate;
  @ApiModelProperty(required = true)
  private final Date updatedDate;
  @ApiModelProperty(required = true)
  private final Double locationLat;
  @ApiModelProperty(required = true)
  private final Double locationLng;
  @ApiModelProperty(required = true)
  private final GeolocationLogEvent event;
  private final Long riderId;
  @ApiModelProperty(required = true)
  private final String riderEmail;
  @ApiModelProperty(required = true)
  private final String riderFirstName;
  @ApiModelProperty(required = true)
  private final String riderLastName;

  public GeolocationLogDto(GeolocationLog geolocationLog) {
    this.id = geolocationLog.getId();
    this.createdDate = geolocationLog.getCreatedDate();
    this.updatedDate = geolocationLog.getUpdatedDate();
    this.locationLat = geolocationLog.getLocationLat();
    this.locationLng = geolocationLog.getLocationLng();
    this.event = geolocationLog.getEvent();
    if (geolocationLog.getRider() != null) {
      this.riderId = geolocationLog.getRider().getId();
      this.riderEmail = geolocationLog.getRider().getEmail();
      this.riderFirstName = geolocationLog.getRider().getFirstname();
      this.riderLastName = geolocationLog.getRider().getLastname();
    } else {
      this.riderId = 0L;
      this.riderEmail = "";
      this.riderFirstName = "";
      this.riderLastName = "";
    }
  }

}
