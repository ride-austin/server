package com.rideaustin.service;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.config.RideJobServiceConfig;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.rating.DriverRatingService;
import com.rideaustin.service.rating.RatingUpdateService;
import com.rideaustin.service.rating.RiderRatingService;
import com.rideaustin.utils.CommentUtils;
import com.rideaustin.utils.RideValidationUtils;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideRatingService {

  private final FareService fareService;
  private final PaymentJobService rideJobService;
  private final RatingUpdateService ratingUpdateService;
  private final DriverRatingService driverRatingService;
  private final RiderRatingService riderRatingService;
  private final EventsNotificationService eventsNotificationService;

  private final RidePaymentConfig config;
  private final RideJobServiceConfig jobsConfig;

  private final RideDslRepository rideDslRepository;

  @Transactional
  public void rateRideAsRider(long rideId, BigDecimal rating, BigDecimal tip, String comment) throws BadRequestException {
    Ride ride = rideDslRepository.findOne(rideId);
    if (ride.getStatus() != RideStatus.COMPLETED) {
      return;
    }
    CommentUtils.validateComment(comment);
    if (ride.getDriverRating() != null || ride.getTip() != null) {
      throw new BadRequestException("This ride has already been rated");
    }
    if (rating != null) {
      updateDriverRating(ride, rating, comment);
    }
    if (tip != null && Money.of(CurrencyUnit.USD, tip).isPositive()) {
      tipRide(ride, tip);
    } else if (tip != null && Money.of(CurrencyUnit.USD, tip).isNegative()) {
      throw new BadRequestException("Tip cannot be less than zero");
    }
    rideJobService.reschedulePaymentJob(ride.getId());
    fareService.calculateTotals(ride);
    rideDslRepository.save(ride);
  }

  @Transactional
  public void rateRideAsDriver(long rideId, BigDecimal rating, String comment) throws RideAustinException {
    Ride ride = rideDslRepository.findOne(rideId);
    if (ride.getStatus() != RideStatus.COMPLETED) {
      return;
    }
    CommentUtils.validateComment(comment);
    if (ride.getRiderRating() != null) {
      throw new BadRequestException("This ride has already been rated");
    }
    if (rating != null) {
      updateRiderRating(ride, rating, comment);
    }
    fareService.calculateTotals(ride);
    rideDslRepository.save(ride);
  }

  private void tipRide(Ride ride, BigDecimal tip) throws BadRequestException {
    RideValidationUtils.validateTippedRide(ride, jobsConfig.getRidePaymentDelay());
    RideValidationUtils.validateTip(tip, config.getTipLimit());
    ride.getFareDetails().setTip(Money.of(CurrencyUnit.USD, tip, Constants.ROUNDING_MODE));
    ride.setTippedOn(new Date());
    rideDslRepository.save(ride);
  }

  private void updateDriverRating(Ride ride, BigDecimal rating, String comment) {
    Driver driver = ride.getActiveDriver().getDriver();
    ride.setDriverRating(rating.doubleValue());
    rideDslRepository.save(ride);
    ratingUpdateService.saveNewRatingUpdate(ride.getActiveDriver().getDriver(), ride.getRider(), rating.doubleValue(), ride, comment);
    driverRatingService.updateDriverRating(driver);
    eventsNotificationService.sendRatingUpdated(driver.getId(), driver.getRating());
  }

  private void updateRiderRating(Ride ride, BigDecimal rating, String comment) {
    Rider rider = ride.getRider();
    ride.setRiderRating(rating.doubleValue());
    rideDslRepository.save(ride);
    ratingUpdateService.saveNewRatingUpdate(ride.getRider(), ride.getActiveDriver().getDriver(), rating.doubleValue(), ride, comment);
    riderRatingService.updateRiderRating(rider);
  }
}
