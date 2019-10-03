package com.rideaustin.service.areaqueue;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.application.cache.CacheItem;
import com.rideaustin.model.Area;
import com.rideaustin.repo.dsl.AreaDslRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class AreaCache implements CacheItem {

  public static final String AREAS_CACHE = "areasCache";
  private final AreaDslRepository areaDslRepository;

  public List<Area> getAreasPerCity(Long cityId) {
    return getAllAreas().stream().filter(a -> Objects.equals(a.getCityId(), cityId)).collect(Collectors.toList());
  }

  public Collection<Area> getAllAreas() {
    return areaDslRepository.findAll();
  }

  @PostConstruct
  @CacheEvict(value = AREAS_CACHE, allEntries = true)
  public void refreshCache() {
    //refresh cache is performed by spring
  }

  public Map getAllCacheItems() {
    return ImmutableMap.of("areas", areaDslRepository.findAll());
  }

  public String getCacheName() {
    return AREAS_CACHE;
  }
}