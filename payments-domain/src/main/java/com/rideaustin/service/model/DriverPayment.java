package com.rideaustin.service.model;

import org.joda.money.Money;

import com.rideaustin.Constants;
import com.rideaustin.model.ride.Ride;

public class DriverPayment {

  private Money ridePayments = Constants.ZERO_USD;

  public void addRidePayment(Ride ride) {
    ridePayments = ridePayments.plus(ride.getDriverPayment());
  }

  public Money getRidePayments() {
    return ridePayments;
  }

}
