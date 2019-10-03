package com.rideaustin.service.city;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideaustin.model.City;
import com.rideaustin.model.CityRestriction;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.CityDriverType;
import com.rideaustin.model.ride.CityDriverType.Configuration;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.repo.dsl.CityRestrictionDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.model.RideStartLocation;
import com.rideaustin.service.CityService;
import com.sromku.polygon.Polygon;

public class DefaultCityValidationServiceTest {

  @Mock
  private CityService cityService;
  @Mock
  private CityRestrictionDslRepository restrictionRepository;
  private ObjectMapper objectMapper = new ObjectMapper();

  private DefaultCityValidationService testedInstance;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DefaultCityValidationService(cityService, restrictionRepository, objectMapper);
  }

  @Test
  public void validateCityForDocumentThrowsExceptionOnNullCityId() throws BadRequestException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Missing city id");

    testedInstance.validateCity(DocumentType.TNC_CARD, null);
  }

  @Test
  public void validateCityForLocationThrowsExceptionOnRequiredValidationAndOutsideCity() throws BadRequestException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Please select a pickup location within ");

    final RideStartLocation rideStartLocation = new RideStartLocation();
    rideStartLocation.setStartLocationLat(34.06461);
    rideStartLocation.setStartLocationLong(-97.648918);
    final CityDriverType driverType = mock(CityDriverType.class);
    Configuration configuration = mock(Configuration.class);
    when(driverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(configuration.isCityValidationRequired()).thenReturn(true);

    final City city = new City();
    final AreaGeometry areaGeometry = new AreaGeometry();
    final Polygon polygon = mock(Polygon.class);
    areaGeometry.setPolygon(polygon);
    city.setAreaGeometry(areaGeometry);
    when(polygon.contains(eq(rideStartLocation.getLat()), eq(rideStartLocation.getLng()))).thenReturn(false);
    when(cityService.getCityOrThrow(anyLong())).thenReturn(city);

    testedInstance.validateCity(rideStartLocation, driverType, 1L);
  }

  @Test
  public void validateCityForLocationThrowsExceptionWhenInsideRestrictedArea() throws BadRequestException {
    expectedException.expect(BadRequestException.class);
    expectedException.expectMessage("Ride can't be requested now. Please try again later");

    final RideStartLocation rideStartLocation = new RideStartLocation();
    rideStartLocation.setStartLocationLat(34.06461);
    rideStartLocation.setStartLocationLong(-97.648918);
    final CityDriverType driverType = mock(CityDriverType.class);
    Configuration configuration = mock(Configuration.class);
    when(driverType.getConfigurationObject(any(ObjectMapper.class))).thenReturn(configuration);
    when(configuration.isCityValidationRequired()).thenReturn(true);

    final City city = new City();
    final AreaGeometry areaGeometry = new AreaGeometry();
    final Polygon polygon = mock(Polygon.class);
    areaGeometry.setPolygon(polygon);
    city.setAreaGeometry(areaGeometry);
    when(polygon.contains(eq(rideStartLocation.getLat()), eq(rideStartLocation.getLng()))).thenReturn(true);
    when(cityService.getCityOrThrow(anyLong())).thenReturn(city);

    final CityRestriction restriction = new CityRestriction();
    final AreaGeometry restrictedZone = new AreaGeometry();
    final Polygon restrictedPolygon = mock(Polygon.class);
    restrictedZone.setPolygon(restrictedPolygon);
    restriction.setAreaGeometry(restrictedZone);
    when(restrictionRepository.findByCity(anyLong())).thenReturn(Collections.singletonList(restriction));
    when(restrictedPolygon.contains(eq(rideStartLocation.getLat()), eq(rideStartLocation.getLng()))).thenReturn(true);

    testedInstance.validateCity(rideStartLocation, driverType, 1L);
  }
}