package com.rideaustin.service.rating;

import com.rideaustin.model.user.Rider;

public interface RiderRatingService extends RatingService<Rider> {
  default void updateRiderRating(Rider rider) {
    rider.setRating(calculateRating(rider));
  }
}
