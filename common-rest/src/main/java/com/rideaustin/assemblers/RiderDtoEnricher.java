package com.rideaustin.assemblers;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.rest.model.RiderDto;
import com.rideaustin.service.user.BlockedDeviceService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class RiderDtoEnricher implements DTOEnricher<RiderDto> {

  private final BlockedDeviceService blockedDeviceService;

  @Override
  public RiderDto enrich(RiderDto source) {
    if (source == null) {
      return null;
    }
    source.getUser().setDeviceBlocked(blockedDeviceService.isInBlocklist(source.getUser().getId()));
    return source;
  }
}
