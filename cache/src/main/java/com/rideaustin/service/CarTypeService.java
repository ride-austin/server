package com.rideaustin.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.enums.ConfigurationWeekday;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.repo.dsl.CarTypeDslRepository;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.utils.DateUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CarTypeService {

  private final ObjectMapper mapper;
  private final CarTypesCache cache;
  private final CarTypeDslRepository repository;

  public List<CityCarType> getCityCarTypes(Long cityId) {
    final List<CityCarType> cityCarTypes = cache.getCityCarTypes(cityId);
    final Calendar calendar = Calendar.getInstance();
    final List<CityCarType> result = new ArrayList<>();
    for (CityCarType type : cityCarTypes) {
      final CityCarType.CityTypeConfiguration configuration = type.getConfigurationObject(mapper);
      if (CollectionUtils.isNotEmpty(configuration.getWeekdays())) {
        final ConfigurationWeekday today = ConfigurationWeekday.fromWeekday(calendar.get(Calendar.DAY_OF_WEEK));
        final Date date = new Date();
        if (configuration.getWeekdays().contains(today) && configuration.getActiveTo() != null && configuration.getActiveFrom() != null
          && DateUtils.isWithinHours(date, configuration.getActiveFrom(), configuration.getActiveTo())) {
          result.add(type);
        }
      } else {
        result.add(type);
      }
    }
    return result;
  }

  public Optional<CityCarType> getCityCarType(@Nonnull CarType carType, @Nonnull Long cityId) {
    return getCityCarType(carType.getCarCategory(), cityId);
  }

  public Optional<CityCarType> getCityCarTypeWithFallback(@Nonnull CarType carType, @Nonnull Long cityId) {
    Optional<CityCarType> cached = getCityCarType(carType.getCarCategory(), cityId);
    if (cached.isPresent()) {
      return cached;
    } else {
      return repository.findByTypeAndCity(carType, cityId);
    }
  }

  public Optional<CityCarType> getCityCarTypeWithFallback(@Nonnull String carType, @Nonnull Long cityId) {
    Optional<CityCarType> cached = getCityCarType(carType, cityId);
    if (cached.isPresent()) {
      return cached;
    } else {
      return repository.findByTypeAndCity(carType, cityId);
    }
  }

  public Optional<CityCarType> getCityCarType(String carCategory, Long cityId) {
    return getCityCarTypes(cityId).stream().filter(ct -> ct.getCarType().getCarCategory().equals(carCategory)).findFirst();
  }
}
