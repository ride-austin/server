package com.rideaustin.assemblers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.statemachine.ExtendedState;
import org.springframework.statemachine.StateMachineContext;
import org.springframework.statemachine.StateMachinePersist;
import org.springframework.stereotype.Component;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.rest.model.MobileRiderRideDto.ActiveDriverDto;
import com.rideaustin.service.CarTypeService;
import com.rideaustin.service.MapService;
import com.rideaustin.service.MobileRiderRideLocationEnricher;
import com.rideaustin.service.UpdateDistanceTimeService;
import com.rideaustin.service.config.RidePaymentConfig;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.model.DispatchCandidate;
import com.rideaustin.service.model.Events;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.model.States;
import com.rideaustin.service.model.context.DispatchContext;
import com.rideaustin.service.model.context.RideFlowContext;
import com.rideaustin.service.ride.RideUpgradeService;
import com.rideaustin.utils.dispatch.StateMachineUtils;

@Primary
@Component
public class MobileRiderRideDtoEnricher<T extends MobileRiderRideDto> extends MobileRiderRideLocationEnricher<T> {

  private final RidePaymentConfig paymentConfig;

  private final DocumentDslRepository documentDslRepository;
  private final CarDocumentDslRepository carDocumentDslRepository;

  private final MapService mapService;
  private final UpdateDistanceTimeService updateDistanceTimeService;

  private final CityCarTypeDtoAssembler cityCarTypeDtoAssembler;
  private final CarTypeService carTypeService;

  @Inject
  public MobileRiderRideDtoEnricher(StateMachinePersist<States, Events, String> contextAccess, Environment environment,
    RideUpgradeService upgradeService, ObjectLocationService<OnlineDriverDto> objectLocationService,
    RideDslRepository rideDslRepository, RidePaymentConfig paymentConfig, DocumentDslRepository documentDslRepository,
    CarDocumentDslRepository carDocumentDslRepository, MapService mapService,
    CityCarTypeDtoAssembler cityCarTypeDtoAssembler, UpdateDistanceTimeService updateDistanceTimeService, CarTypeService carTypeService) {
    super(contextAccess, environment, upgradeService, objectLocationService, rideDslRepository);
    this.paymentConfig = paymentConfig;
    this.documentDslRepository = documentDslRepository;
    this.carDocumentDslRepository = carDocumentDslRepository;
    this.mapService = mapService;
    this.cityCarTypeDtoAssembler = cityCarTypeDtoAssembler;
    this.updateDistanceTimeService = updateDistanceTimeService;
    this.carTypeService = carTypeService;
  }

  @Override
  public T enrich(T source) {
    final T enriched = super.enrich(source);
    if (enriched == null) {
      return null;
    }
    setTip(enriched);
    setCarType(enriched);

    if (!paymentConfig.isUpfrontPricingEnabled()) {
      enriched.setUpfrontCharge(null);
    }

    StateMachineContext<States, Events> persistedContext = StateMachineUtils.getPersistedContext(environment, contextAccess, enriched.getId());
    if (persistedContext != null) {
      ExtendedState extendedState = persistedContext.getExtendedState();
      RideFlowContext flowContext = StateMachineUtils.getFlowContext(extendedState);
      DispatchContext dispatchContext = StateMachineUtils.getDispatchContext(extendedState);
      enriched.setDriverAcceptedOn(flowContext.getAcceptedOn());
      DispatchCandidate candidate = Optional.ofNullable(dispatchContext).map(DispatchContext::getCandidate).orElse(null);
      setETAAndFreeCancellation(enriched, flowContext, candidate);
    }

    setETC(enriched);
    setPhotos(enriched);

    return enriched;
  }

  private void setETAAndFreeCancellation(MobileRiderRideDto source, RideFlowContext flowContext, DispatchCandidate candidate) {
    if (candidate != null) {
      boolean freeCancellationExpiresShouldBeSet = false;
      Date driverAcceptedOn = source.getDriverAcceptedOn();
      ActiveDriverDto activeDriver = source.getActiveDriver();
      if (flowContext.isStacked()) {
        MobileRiderRideDto.PrecedingRide precedingRide = rideDslRepository.findPrecedingRide(candidate.getId());
        source.setPrecedingRide(precedingRide);
        if (precedingRide == null && driverAcceptedOn != null) {
          freeCancellationExpiresShouldBeSet = true;
        } else {
          activeDriver.setDrivingTimeToRider(updateDistanceTimeService.getDrivingTimeWithETC(source.getId(), activeDriver.getId(),
            source.getStartLocationLat(), source.getStartLocationLong(), activeDriver.getLatitude(), activeDriver.getLongitude()));
        }
      } else if (driverAcceptedOn != null) {
        freeCancellationExpiresShouldBeSet = true;
      }
      if (freeCancellationExpiresShouldBeSet) {
        source.setFreeCancellationExpiresOn(Date.from(driverAcceptedOn.toInstant().plus(paymentConfig.getCancellationChargeFreePeriod(), ChronoUnit.SECONDS)));
        //eta should be calculated without ETC when ride is stacked and previous is completed or when ride is regular
        //quite the same as `freeCancellationExpiresShouldBeSet`
        if (source.getStatus() == RideStatus.DRIVER_ASSIGNED) {
          activeDriver.setDrivingTimeToRider(mapService.getTimeToDriveCached(source.getId(),
            new LatLng(activeDriver.getLatitude(), activeDriver.getLongitude()),
            new LatLng(source.getStartLocationLat(), source.getStartLocationLong())));
        }
      }
      source.setEstimatedTimeArrive(candidate.getDrivingTimeToRider());
    }
  }

  private void setCarType(MobileRiderRideDto source) {
    if (source.getCarType() != null) {
      carTypeService.getCityCarTypeWithFallback(source.getCarType(), source.getCityId())
        .map(cityCarTypeDtoAssembler::toDto)
        .ifPresent(source::setRequestedCarType);
    }
  }

  private void setTip(MobileRiderRideDto source) {
    if (source.getDriverRating() != null && source.getTip() == null) {
      source.setTip(Constants.ZERO_USD);
    }
  }

  private void setPhotos(MobileRiderRideDto source) {
    if (source.getActiveDriver() != null) {
      Document photo = documentDslRepository.findByAvatarAndType(source.getActiveDriver().getDriver().getId(), DocumentType.DRIVER_PHOTO);
      if (photo != null) {
        source.getActiveDriver().getDriver().setPhotoUrl(photo.getDocumentUrl());
      }
      final ActiveDriverDto.MobileRiderCarDto selectedCar = source.getActiveDriver().getSelectedCar();
      if (selectedCar != null) {
        final long carId = selectedCar.getId();
        final Map<DocumentType, String> carPhotos = carDocumentDslRepository.findCarPhotos(carId)
          .stream()
          .collect(Collectors.toMap(Document::getDocumentType, Document::getDocumentUrl, (k, v) -> v));
        String mainCarPhoto = carPhotos.get(DocumentType.CAR_PHOTO_FRONT);
        if (mainCarPhoto != null) {
          selectedCar.setPhotoUrl(mainCarPhoto);
        }
        selectedCar.setCarPhotos(carPhotos);
      }
    }
  }

  private void setETC(MobileRiderRideDto source) {
    if (source.getStatus() == RideStatus.ACTIVE && source.getEndLocationLat() != null && source.getEndLocationLong() != null) {
      Optional.ofNullable(
        mapService.getTimeToDriveCached(
          source.getId(),
          new LatLng(source.getActiveDriver().getLatitude(), source.getActiveDriver().getLongitude()),
          new LatLng(source.getEndLocationLat(), source.getEndLocationLong())
        )
      )
        .map(e -> Instant.now().plus(e, ChronoUnit.SECONDS))
        .map(Date::from)
        .ifPresent(source::setEstimatedTimeCompletion);
    }
  }
}
