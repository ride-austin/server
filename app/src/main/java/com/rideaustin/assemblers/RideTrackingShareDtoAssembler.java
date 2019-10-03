package com.rideaustin.assemblers;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.rideaustin.Constants;
import com.rideaustin.model.Address;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.HeadingDirection;
import com.rideaustin.rest.model.Location;
import com.rideaustin.rest.model.RideTrackingShareDto;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.utils.DateUtils;
import com.rideaustin.utils.dispatch.StateMachineUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RideTrackingShareDtoAssembler implements SingleSideAssembler<Ride, RideTrackingShareDto> {

  private final CarTypesCache carTypesCache;
  private final DocumentService documentService;
  private final StateMachinePersist<States, Events, String> contextAccess;
  private final Environment environment;

  @Override
  public RideTrackingShareDto toDto(Ride ride) {
    RideTrackingShareDto rideTrackingShareDto = new RideTrackingShareDto();

    rideTrackingShareDto.setRideId(ride.getId());
    rideTrackingShareDto.setCityId(ride.getCityId());

    rideTrackingShareDto.setRiderId(ride.getRider().getId());
    rideTrackingShareDto.setRiderFirstName(ride.getRider().getUser().getFirstname());
    rideTrackingShareDto.setRiderLastName(ride.getRider().getUser().getLastname());
    rideTrackingShareDto.setRiderPhoto(ride.getRider().getUser().getPhotoUrl());

    rideTrackingShareDto.setRideCarCategory(ride.getRequestedCarType().getCarCategory());

    ActiveDriver activeDriver = ride.getActiveDriver();
    if (activeDriver != null) {
      Driver driver = activeDriver.getDriver();
      rideTrackingShareDto.setDriverId(driver.getId());
      rideTrackingShareDto.setDriverFirstName(driver.getUser().getFirstname());
      rideTrackingShareDto.setDriverLastName(driver.getUser().getLastname());
      String photoUrl = Optional.ofNullable(documentService.findAvatarDocument(driver, DocumentType.DRIVER_PHOTO)).map(Document::getDocumentUrl).orElse("");
      rideTrackingShareDto.setDriverPhoto(photoUrl);
      rideTrackingShareDto.setDriverRating(driver.getRating());
      Optional<Car> car = driver.getCars().stream().filter(Car::isSelected).findFirst();
      rideTrackingShareDto.setDriverCar(car.orElse(null));
      rideTrackingShareDto.setDriverCarTypes(car.map(c -> carTypesCache.fromBitMask(c.getCarCategoriesBitmask())).orElse(null));
      rideTrackingShareDto.setDriverLicensePlate(car.map(Car::getLicense).orElse(null));
      if (RideStatus.ACTIVE.equals(ride.getStatus())) {
        rideTrackingShareDto.setCurrentSpeed(activeDriver.getSpeed());
        rideTrackingShareDto.setCurrentCourse(activeDriver.getCourse());
        Double heading = activeDriver.getHeading();
        rideTrackingShareDto.setCurrentHeading(heading);
        rideTrackingShareDto.setCurrentHeadingDirection(HeadingDirection.from(heading));
        rideTrackingShareDto.setDriverLocation(new Location(activeDriver.getLatitude(), activeDriver.getLongitude()));
      }
    }

    rideTrackingShareDto.setStatus(ride.getStatus());
    if (RideStatus.COMPLETED.equals(ride.getStatus())) {
      rideTrackingShareDto.setCompletedOn(Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(ride.getCompletedOn())));
    }
    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, contextAccess, ride.getId());
    Date startedOn = ride.getStartedOn();
    if (persistedContext != null && startedOn == null) {
      ExtendedState extendedState = persistedContext.getExtendedState();
      final RideFlowContext flowContext = StateMachineUtils.getFlowContext(extendedState);
      startedOn = flowContext.getStartedOn();
    }
    Optional.ofNullable(startedOn).map(DateUtils::dateToInstant).map(Constants.DATETIME_FORMATTER::format).ifPresent(rideTrackingShareDto::setStartedOn);
    rideTrackingShareDto.setCreatedDate(Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(ride.getCreatedDate())));
    rideTrackingShareDto.setUpdatedDate(Constants.DATETIME_FORMATTER.format(DateUtils.dateToInstant(ride.getUpdatedDate())));

    Optional.ofNullable(ride.getStart()).map(Address::getAddress).ifPresent(rideTrackingShareDto::setStartAddress);
    if (ride.getStartLocationLat() != null && ride.getStartLocationLong() != null) {
      rideTrackingShareDto.setStartLocation(new Location(ride.getStartLocationLat(), ride.getStartLocationLong()));
    }
    Optional.ofNullable(ride.getEnd()).map(Address::getAddress).ifPresent(rideTrackingShareDto::setEndAddress);
    if (ride.getEndLocationLat() != null && ride.getEndLocationLong() != null) {
      rideTrackingShareDto.setEndLocation(new Location(ride.getEndLocationLat(), ride.getEndLocationLong()));
    }
    return rideTrackingShareDto;
  }
}
