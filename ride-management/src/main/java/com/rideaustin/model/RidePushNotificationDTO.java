package com.rideaustin.model;

import org.joda.money.Money;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.user.User;

import lombok.Getter;

@Getter
public class RidePushNotificationDTO {

  private final long id;
  private final String driverName;
  private final RideStatus status;
  private final Money cancellationFee;
  private final User user;

  @QueryProjection
  public RidePushNotificationDTO(long id, String driverName, RideStatus status, Money cancellationFee, User user) {
    this.id = id;
    this.driverName = driverName;
    this.status = status;
    this.cancellationFee = cancellationFee;
    this.user = user;
  }
}
