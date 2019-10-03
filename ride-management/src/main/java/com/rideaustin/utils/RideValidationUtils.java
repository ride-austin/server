package com.rideaustin.utils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.exception.BadRequestException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RideValidationUtils {

  private RideValidationUtils() {}

  public static void validateTip(BigDecimal tip, BigDecimal limit) throws BadRequestException {
    if (tip.compareTo(limit) > 0) {
      throw new BadRequestException("Tip cannot be more than " + limit);
    }
  }

  public static void validateTippedRide(Ride ride, int delay) throws BadRequestException {
    Instant tippingTimeThreshold =
      Instant.now().minus(delay, ChronoUnit.SECONDS);

    if (tippingTimeThreshold.isAfter(ride.getCompletedOn().toInstant())) {
      throw new BadRequestException("Tipping period has passed");
    }

    if (ride.getPaymentStatus() == PaymentStatus.PAID) {
      throw new BadRequestException("Ride has already been charged");
    }
  }

}
