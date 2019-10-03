package com.rideaustin.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.Constants;
import com.rideaustin.filter.ClientAppVersion;
import com.rideaustin.filter.ClientAppVersionContext;
import com.rideaustin.model.City;
import com.rideaustin.rest.exception.BadRequestException;

@RunWith(MockitoJUnitRunner.class)
public class CityServiceTest {

  @Mock
  private CityCache cityCache;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private CityService testedInstance;

  private List<City> cities = new ArrayList<>();

  @Before
  public void setup() {

    cities.add(mockCity(1L));
    cities.add(mockCity(2L));

    when(cityCache.getAllCities()).thenReturn(cities);

    testedInstance = new CityService(cityCache);

  }

  @Test
  public void testGetAllIds() {
    List<Long> citiesIds = testedInstance.getCitiesIds();
    assertThat(citiesIds.size(), is(2));

    Long toTest0 = 0L;
    Long toTest1 = 1L;
    Long toTest3 = 3L;

    assertThat("cities do not have 0", !citiesIds.contains(toTest0));
    assertThat("cities have 1 ", citiesIds.contains(toTest1));
    assertThat("cities do not have 3", !citiesIds.contains(toTest3));
  }

  @Test
  public void getByIdReturnsDefaultWhenIdIsNull() {
    when(cityCache.getCity(Constants.DEFAULT_CITY_ID)).thenReturn(cities.get(0));
    final City result = testedInstance.getById(null);

    assertEquals(Constants.DEFAULT_CITY_ID, result.getId());
  }

  @Test
  public void getCityOrThrowThrowsErrorWhenCityNotFound() throws BadRequestException {
    when(cityCache.getCity(anyLong())).thenReturn(null);

    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Invalid city id");

    testedInstance.getCityOrThrow(6L);
  }

  @Test
  public void getCityForCurrentClientAppVersionContextReturnsDefaultForAbsentVersion() {
    when(cityCache.getCity(anyLong())).thenReturn(cities.get(0));

    final City result = testedInstance.getCityForCurrentClientAppVersionContext();

    assertEquals(cities.get(0), result);
  }

  @Test
  public void getCityForCurrentClientAppVersionContextReturnsDefaultForAbsentUserAgent() {
    when(cityCache.getCity(anyLong())).thenReturn(cities.get(0));
    ClientAppVersionContext.setClientAppVersion(new ClientAppVersion());

    final City result = testedInstance.getCityForCurrentClientAppVersionContext();

    assertEquals(cities.get(0), result);
  }

  @Test
  public void getCityForCurrentClientAppVersionContextReturnsDefaultForAustinUserAgent() {
    when(cityCache.getCity(1L)).thenReturn(cities.get(0));
    when(cityCache.getCity(2L)).thenReturn(cities.get(1));
    final ClientAppVersion appVersion = new ClientAppVersion();
    appVersion.setUserAgent("RideAustin");
    ClientAppVersionContext.setClientAppVersion(appVersion);

    final City result = testedInstance.getCityForCurrentClientAppVersionContext();

    assertEquals(cities.get(0), result);
  }

  @Test
  public void getCityForCurrentClientAppVersionContextReturnsDefaultForHoustonUserAgent() {
    when(cityCache.getCity(1L)).thenReturn(cities.get(0));
    when(cityCache.getCity(2L)).thenReturn(cities.get(1));
    final ClientAppVersion appVersion = new ClientAppVersion();
    appVersion.setUserAgent("RideHouston");
    ClientAppVersionContext.setClientAppVersion(appVersion);

    final City result = testedInstance.getCityForCurrentClientAppVersionContext();

    assertEquals(cities.get(1), result);
  }

  private City mockCity(Long id) {
    City city = new City();
    city.setId(id);
    city.setEnabled(true);
    return city;

  }

}
