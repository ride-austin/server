package com.rideaustin.rest.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.service.location.model.LocationObject;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@ApiModel
public class CompactActiveDriverDto {

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  private final long id;
  @ApiModelProperty(required = true)
  private double latitude;
  @ApiModelProperty(required = true)
  private double longitude;
  @ApiModelProperty
  private Double speed;
  @ApiModelProperty
  private Double course;
  @Setter
  @ApiModelProperty(required = true)
  private ActiveDriverStatus status;
  @ApiModelProperty(required = true)
  private final DriverDto driver;
  @Setter
  @ApiModelProperty
  private Long drivingTimeToRider;
  @Setter
  @ApiModelProperty
  private MobileDriverRideDto ride;

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public CompactActiveDriverDto(@JsonProperty("id") long id, @JsonProperty("driver") DriverDto driver) {
    this.id = id;
    this.driver = driver;
  }

  public CompactActiveDriverDto(long id, long driverId, long userId, ActiveDriverStatus status) {
    this.id = id;
    this.status = status;
    this.driver = new DriverDto(driverId, userId);
  }

  public void setLocation(LocationObject location) {
    this.course = location.getCourse();
    this.latitude = location.getLatitude();
    this.longitude = location.getLongitude();
    this.speed = location.getSpeed();
  }

  @Getter
  @ApiModel
  public static class DriverDto {
    @ApiModelProperty(required = true)
    private final long id;
    @ApiModelProperty(required = true)
    private final User user;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public DriverDto(@JsonProperty("id") long id, @JsonProperty("user") User user) {
      this.id = id;
      this.user = user;
    }

    DriverDto(long id, long userId) {
      this.id = id;
      this.user = new User(userId);
    }

    @Getter
    @ApiModel
    public static class User {
      @ApiModelProperty(required = true)
      private final long id;

      @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
      public User(@JsonProperty("id") long id) {
        this.id = id;
      }
    }

  }

}