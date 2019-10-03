package com.rideaustin.service.airport;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

import com.rideaustin.application.cache.CacheItem;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.repo.dsl.AirportDslRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AirportCache implements CacheItem {

  public static final String AIRPORTS_CACHE = "airportsCache";

  private final AirportDslRepository airportDslRepository;

  public Map<Long, Airport> getAirports() {
    return airportDslRepository.findAll().stream().collect(Collectors.toMap(Airport::getId, Function.identity()));
  }

  @Override
  @PostConstruct
  @CacheEvict(value = AIRPORTS_CACHE, allEntries = true)
  public void refreshCache() {
    //do nothing
  }

  @Override
  public Map getAllCacheItems() {
    return getAirports();
  }

  @Override
  public String getCacheName() {
    return AIRPORTS_CACHE;
  }
}