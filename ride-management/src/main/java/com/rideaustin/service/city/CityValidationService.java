package com.rideaustin.service.city;

import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.model.RideStartLocation;

public interface CityValidationService {
  void validateCity(DocumentType documentType, Long cityId) throws BadRequestException;
  void validateCity(RideStartLocation startLocation, CityDriverType driverType, Long cityId) throws BadRequestException;
}
