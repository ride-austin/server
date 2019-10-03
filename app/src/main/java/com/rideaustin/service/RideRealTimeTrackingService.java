package com.rideaustin.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.EmailException;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.model.City;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.ForbiddenException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.Location;
import com.rideaustin.rest.model.TrackingShareToken;
import com.rideaustin.service.email.EmailService;
import com.rideaustin.service.ride.RideOwnerService;
import com.rideaustin.service.ride.TrackingInvitationEmail;
import com.rideaustin.utils.RandomString;

@Service
@Transactional
public class RideRealTimeTrackingService {

  private static final String DEFAULT_TRACKING_URL = "/real-time-tracking?id=";

  private final RideDslRepository rideDslRepository;
  private final CurrentUserService currentUserService;
  private final RideOwnerService rideOwnerService;
  private final RideTrackerService rideTrackerService;
  private final EmailService emailService;
  private final CityService cityService;

  private String trackingUrl;
  private String currentAPI;

  @Inject
  public RideRealTimeTrackingService(RideDslRepository rideDslRepository, CurrentUserService currentUserService,
    RideOwnerService rideOwnerService, RideTrackerService rideTrackerService, Environment environment, EmailService emailService, CityService cityService) {
    this.rideDslRepository = rideDslRepository;
    this.currentUserService = currentUserService;
    this.rideOwnerService = rideOwnerService;
    this.rideTrackerService = rideTrackerService;
    this.emailService = emailService;
    this.cityService = cityService;

    trackingUrl = environment.getProperty("tracking.share.return.url", DEFAULT_TRACKING_URL);
    currentAPI = environment.getProperty("ra.project.api-suffix", "");
  }

  public void shareRideToFollow(Long rideId, String recipient) throws RideAustinException {
    User user = currentUserService.getUser();
    Ride ride = generateOrReturnToken(rideId, currentUserService.getUser());
    City city = cityService.getById(ride.getCityId());
    String urlToSend = city.getPageUrl().concat(trackingUrl).concat(ride.getTrackingShareToken());
    if (StringUtils.isNotBlank(currentAPI)) {
      urlToSend = urlToSend.concat("&env=").concat(currentAPI);
    }
    try {
      emailService.sendEmail(new TrackingInvitationEmail(user, city, urlToSend, recipient));
    } catch (EmailException e) {
      throw new ServerError(e);
    }
  }

  public TrackingShareToken getShareToken(Long rideId) throws RideAustinException {
    Ride ride = generateOrReturnToken(rideId, currentUserService.getUser());
    return new TrackingShareToken(ride.getTrackingShareToken());
  }

  public Ride getRideForTrackingShareToken(String trackingKey) throws NotFoundException {
    return Optional.ofNullable(rideDslRepository.getRideForTrackingShareToken(trackingKey)).orElseThrow(() -> new NotFoundException("Ride not found"));
  }

  public List<Location> getCurrentRideTrackingLocations(Ride ride) {
    List<RideTracker> trackers = rideTrackerService.getTrackersForRide(ride);
    return trackers.stream()
      .map(tracker -> new Location(tracker.getLatitude(), tracker.getLongitude())).collect(Collectors.toList());
  }

  private Ride generateOrReturnToken(Long rideId, User currentUser) throws NotFoundException, ForbiddenException {
    Ride ride = rideDslRepository.findOne(rideId);
    if (ride == null) {
      throw new NotFoundException("This ride does not exist");
    }

    boolean isAdminOrPartOfRide = !currentUser.isAdmin()
      && !rideOwnerService.isRideRider(rideId)
      && !rideOwnerService.isDriversRide(rideId);

    if (isAdminOrPartOfRide) {
      throw new ForbiddenException();
    }
    if (ride.getTrackingShareToken() == null) {
      ride.setTrackingShareToken(RandomString.generate(10));
      rideDslRepository.save(ride);
    }
    return ride;
  }

}
