package com.rideaustin.service.rating;

import com.rideaustin.model.user.Driver;

public interface DriverRatingService extends RatingService<Driver> {

  default void updateDriverRating(Driver driver) {
    driver.setRating(calculateRating(driver));
  }

}
