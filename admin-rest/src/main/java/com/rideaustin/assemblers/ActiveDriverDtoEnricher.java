package com.rideaustin.assemblers;

import javax.inject.Inject;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.rideaustin.model.Session;
import com.rideaustin.rest.model.ActiveDriverDto;
import com.rideaustin.service.CurrentSessionService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.utils.AppInfoUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class ActiveDriverDtoEnricher implements DTOEnricher<ActiveDriverDto>, Converter<ActiveDriverDto, ActiveDriverDto> {

  private final CurrentSessionService currentSessionService;
  private final CarTypesCache carTypesCache;

  @Override
  public ActiveDriverDto enrich(ActiveDriverDto source) {
    Session currentSession = currentSessionService.getCurrentSession(source.getUserId());
    if (currentSession != null) {
      source.setAppVersion(AppInfoUtils.extractVersion(currentSession.getUserAgent()));
    }
    source.setCarCategories(carTypesCache.fromBitMask(source.getAvailableCarCategories()));
    return source;
  }

  @Override
  public ActiveDriverDto convert(ActiveDriverDto source) {
    return enrich(source);
  }
}
