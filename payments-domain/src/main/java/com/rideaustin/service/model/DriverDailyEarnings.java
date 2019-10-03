package com.rideaustin.service.model;

import org.joda.money.Money;

import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;

public class DriverDailyEarnings {

  private int rideCount = 0;
  private Money baseFare = Constants.ZERO_USD;
  private Money distanceFare = Constants.ZERO_USD;
  private Money timeFare = Constants.ZERO_USD;
  private Money totalFare = Constants.ZERO_USD;
  private Money earning = Constants.ZERO_USD;
  private Money raFee = Constants.ZERO_USD;
  private Money tips = Constants.ZERO_USD;

  public void addRide(Ride ride) {
    rideCount++;
    if (ride.getStatus().equals(RideStatus.COMPLETED)) {
      baseFare = baseFare.plus(ride.getBaseFare());
      distanceFare = distanceFare.plus(ride.getDistanceFare());
      timeFare = timeFare.plus(ride.getTimeFare());
      if (ride.getTip() != null) {
        tips = tips.plus(ride.getTip());
      }
    
    }
    raFee = raFee.plus(ride.getRaPayment());
    totalFare = totalFare.plus(ride.getSubTotal());
    earning = earning.plus(ride.getDriverPayment());

  }

  public int getRideCount() {
    return rideCount;
  }

  public Money getBaseFare() {
    return baseFare;
  }

  public Money getDistanceFare() {
    return distanceFare;
  }

  public Money getTimeFare() {
    return timeFare;
  }

  public Money getTotalFare() {
    return totalFare;
  }

  public Money getRideAustinFee() {
    return raFee;
  }

  public Money getEarning() {
    return earning;
  }

  public Money getTips() {
    return tips;
  }
}
