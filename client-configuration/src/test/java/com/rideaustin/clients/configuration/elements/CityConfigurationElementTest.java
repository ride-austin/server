package com.rideaustin.clients.configuration.elements;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.maps.model.LatLng;
import com.rideaustin.clients.configuration.elements.CityConfigurationElement.FullCityInfo;
import com.rideaustin.clients.configuration.elements.CityConfigurationElement.ShortCityInfo;
import com.rideaustin.model.City;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.service.CityService;

public class CityConfigurationElementTest {

  @Mock
  private CityService cityService;

  private CityConfigurationElement testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new CityConfigurationElement(cityService);
  }

  @Test
  public void getConfigurationIncludesOnlyEnabledCities() {
    final City enabledCity = createCity();
    final City disabledCity = new City();
    disabledCity.setEnabled(false);
    when(cityService.findAll()).thenReturn(Arrays.asList(
      enabledCity, disabledCity
    ));
    when(cityService.getById(eq(enabledCity.getId()))).thenReturn(enabledCity);

    final Map configuration = testedInstance.getConfiguration(null, null, enabledCity.getId());

    assertTrue(configuration.containsKey("supportedCities"));
    final List<ShortCityInfo> supportedCities = (List<ShortCityInfo>) configuration.get("supportedCities");
    assertEquals(1, supportedCities.size());
    assertEquals(enabledCity.getName(), supportedCities.get(0).getCityName());
    assertEquals(enabledCity.getId(), supportedCities.get(0).getCityId());
    assertEquals(enabledCity.getLogoUrl(), supportedCities.get(0).getLogoUrl());
    assertEquals(enabledCity.getLogoUrlDark(), supportedCities.get(0).getLogoBlackUrl());
  }

  @Test
  public void getConfigurationFillsFullCityInfo() {
    final City city = createCity();
    when(cityService.findAll()).thenReturn(Collections.singletonList(city));
    when(cityService.getById(eq(city.getId()))).thenReturn(city);

    final Map configuration = testedInstance.getConfiguration(null, null, city.getId());

    assertTrue(configuration.containsKey("currentCity"));
    final FullCityInfo currentCity = (FullCityInfo) configuration.get("currentCity");
    assertEquals(city.getId(), currentCity.getCityId());
    assertEquals(city.getName(), currentCity.getCityName());
    assertEquals(String.format("%s %s", city.getAreaGeometry().getCenterPointLat(), city.getAreaGeometry().getCenterPointLng()), currentCity.getCityCenterLocation());
    assertEquals(new LatLng(city.getAreaGeometry().getCenterPointLat(), city.getAreaGeometry().getCenterPointLng()).toString(), currentCity.getCityCenterLocationData().toString());
    assertTrue(CollectionUtils.isNotEmpty(currentCity.getCityBoundaryPolygon()));
  }

  private City createCity() {
    final City enabledCity = new City();
    enabledCity.setId(1L);
    enabledCity.setName("City");
    enabledCity.setLogoUrl("Url");
    enabledCity.setLogoUrlDark("UrlDark");
    final AreaGeometry areaGeometry = new AreaGeometry();
    areaGeometry.setCsvGeometry("-97.1681,34.9819818 -97.61651,34.64891, -97.68419,34.98419");
    areaGeometry.setCenterPointLat(34.9819818);
    areaGeometry.setCenterPointLng(-97.1681);
    enabledCity.setAreaGeometry(areaGeometry);
    enabledCity.setEnabled(true);
    return enabledCity;
  }
}