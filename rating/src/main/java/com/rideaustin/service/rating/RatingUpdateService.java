package com.rideaustin.service.rating;

import java.util.List;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Avatar;
import com.rideaustin.model.user.RatingUpdate;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RatingUpdateDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.model.RatingUpdateDto;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RatingUpdateService {

  private final RatingUpdateDslRepository ratingUpdateDslRepository;
  private final DriverRatingService driverRatingService;
  private final RiderRatingService riderRatingService;
  private final RiderDslRepository riderDslRepository;
  private final DriverDslRepository driverDslRepository;

  @Transactional(propagation = Propagation.REQUIRED)
  public RatingUpdate saveNewRatingUpdate(@Nonnull Avatar target, @Nonnull Avatar source,
    @Nonnull Double rating, Ride ride, String comment) {
    RatingUpdate ratingUpdate = new RatingUpdate();
    ratingUpdate.setRatedAvatar(target);
    ratingUpdate.setRatedByAvatar(source);
    ratingUpdate.setComment(comment);
    ratingUpdate.setRating(rating);
    ratingUpdate.setRide(ride);
    ratingUpdateDslRepository.save(ratingUpdate);
    return ratingUpdate;
  }

  public List<RatingUpdateDto> getRatingUpdateFor(@Nonnull Avatar avatar) {
    return ratingUpdateDslRepository.getRatingsFor(avatar.getId());
  }

  public List<RatingUpdateDto> getRatingUpdateBy(@Nonnull Avatar avatar) {
    return ratingUpdateDslRepository.getRatingsBy(avatar.getId());
  }

  public RatingUpdateDto updateRating(long id, double value) throws BadRequestException {
    final RatingUpdate rating = ratingUpdateDslRepository.findOne(id);
    if (rating == null) {
      throw new BadRequestException("The ride is not yet rated, you can't override rating");
    }
    rating.setRating(value);
    ratingUpdateDslRepository.save(rating);
    return ratingUpdateDslRepository.findInfo(id);
  }

  public void deleteRating(long id) {
    ratingUpdateDslRepository.delete(id);
  }

  public void recalculate(long id, AvatarType type) {
    if (type == AvatarType.RIDER) {
      riderRatingService.updateRiderRating(riderDslRepository.getRider(id));
    } else if (type == AvatarType.DRIVER) {
      driverRatingService.updateDriverRating(driverDslRepository.findById(id));
    }
  }
}
