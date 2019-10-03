package com.rideaustin.service.rating;

public interface RatingService<T> {

  Double calculateRating(T object);

  Double getDefaultRating();

}
