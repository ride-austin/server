package com.rideaustin.service.ride;

import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.Constants;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.redis.RedisSurgeArea;
import com.rideaustin.model.ride.DriverType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.UnAuthorizedException;
import com.rideaustin.rest.model.Location;
import com.rideaustin.service.CityService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.location.ObjectLocationService;
import com.rideaustin.service.location.enums.LocationType;
import com.rideaustin.service.model.DirectConnectDriverDto;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.surgepricing.SurgePricingService;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.utils.DirectConnectUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DirectConnectService {

  private final DriverDslRepository driverDslRepository;
  private final DriverService driverService;
  private final DocumentService documentService;
  private final SurgePricingService surgePricingService;
  private final CityService cityService;
  private final ObjectLocationService<OnlineDriverDto> objectLocationService;
  private final CurrentUserService currentUserService;
  private final DriverTypeCache driverTypeCache;

  public String updateDirectConnectId(long id) throws RideAustinException {
    String currentDCID = driverDslRepository.getLastDCID();
    String nextId = DirectConnectUtils.generateNextId(currentDCID);
    Driver driver = driverService.findDriver(id);
    if (driver.getUser().equals(currentUserService.getUser())) {
      try {
        driver.setDirectConnectId(nextId);
        driverDslRepository.saveAs(driver, currentUserService.getUser());
        return nextId;
      } catch (Exception e) {
        log.error("Failed to assign DCID", e);
        throw new BadRequestException("Please try again!");
      }
    } else {
      throw new UnAuthorizedException();
    }
  }

  public void validateDirectConnectId(String directConnectId) throws BadRequestException {
    Driver driver = driverDslRepository.findByDirectConnectId(directConnectId);
    if (driver != null) {
      throw new BadRequestException("This direct connect ID is already in use!");
    }
  }

  public DirectConnectDriverDto findDriverForDirectConnect(String id, Double lat, Double lng) throws NotFoundException {
    DirectConnectDriverDto connectDriverDto = driverDslRepository.findByDirectConnectId(id, driverTypeCache.toBitMask(Collections.singleton(DriverType.DIRECT_CONNECT)));
    if (connectDriverDto == null) {
      throw new NotFoundException("Driver not found");
    }
    Document photo = documentService.findAvatarDocument(connectDriverDto.getDriverId(), DocumentType.DRIVER_PHOTO);
    if (photo != null) {
      connectDriverDto.setPhotoUrl(photo.getDocumentUrl());
    }

    OnlineDriverDto location = objectLocationService.getById(connectDriverDto.getId(), LocationType.ACTIVE_DRIVER);
    Double resolvedLat = lat;
    Double resolvedLng = lng;
    if (lat == null || lng == null) {
      if (location == null) {
        throw new NotFoundException("Driver is offline");
      }
      resolvedLat = location.getLatitude();
      resolvedLng = location.getLongitude();
    }
    connectDriverDto.setCategories(CarTypesUtils.fromBitMask(location.getAvailableCarCategoriesBitmask()));
    Long city = cityService.findClosestByCoordinates(new Location(resolvedLat, resolvedLng)).getId();
    for (String category : connectDriverDto.getCategories()) {
      Optional<RedisSurgeArea> area = surgePricingService.getSurgeAreaByCarType(resolvedLat, resolvedLng, city, CarTypesUtils.getCarType(category));
      connectDriverDto.getFactors().put(category, area.map(a -> a.getSurgeFactor(category)).orElse(Constants.NEUTRAL_SURGE_FACTOR));
    }
    return connectDriverDto;
  }
}
