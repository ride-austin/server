package com.rideaustin.assemblers;

import org.springframework.stereotype.Component;

import com.rideaustin.rest.model.ActiveDriverLocationDto;
import com.rideaustin.service.model.OnlineDriverDto;
import com.rideaustin.service.user.CarTypesUtils;

@Component
public class ActiveDriverLocationDtoAssembler implements SingleSideAssembler<OnlineDriverDto, ActiveDriverLocationDto> {
  @Override
  public ActiveDriverLocationDto toDto(OnlineDriverDto onlineDriverDto) {
    if (onlineDriverDto == null) {
      return null;
    }
    return new ActiveDriverLocationDto(onlineDriverDto.getLatitude(), onlineDriverDto.getLongitude(), CarTypesUtils.fromBitMask(onlineDriverDto.getAvailableCarCategoriesBitmask()));
  }
}
