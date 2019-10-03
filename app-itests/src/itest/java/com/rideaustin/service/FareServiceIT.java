package com.rideaustin.service;

import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Optional;

import javax.inject.Inject;

import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.rest.model.EstimatedFareDTO;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.test.LocationProvider;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.util.TestUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles({"dev","itest"})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class FareServiceIT {

  @Inject
  private CarTypeService carTypeService;
  @Inject
  private AirportService airportService;
  @Inject
  private FareEstimateService fareService;
  @Inject
  private LocationProvider locationProvider;

  private CityCarType cityCarType;
  private LatLng airportLocation;
  private LatLng outsideAirportLocation;
  private LatLng center;

  @Before
  public void setUp() throws Exception {
    cityCarType = carTypeService.getCityCarType(TestUtils.REGULAR, Constants.City.AUSTIN.getId())
      .orElseThrow(IllegalArgumentException::new);
    airportLocation = locationProvider.getAirportLocation();
    outsideAirportLocation = locationProvider.getOutsideAirportLocation();
    center = locationProvider.getCenter();
  }

  @Test
  public void testEstimateFareWithAirportFee() throws Exception {
    /*
     * Ride fare estimation with pickup location INSIDE airport zone
     */
    Optional<EstimatedFareDTO> fareOptional1 =
      fareService.estimateFare(airportLocation, center, cityCarType.getCarType(), Constants.City.AUSTIN.getId());

    if (!fareOptional1.isPresent()) {
      fail("Failed to estimate fare");
    }

    EstimatedFareDTO fare1 = fareOptional1.get();

    /*
     * Ride fare estimation with pickup location OUTSIDE of airport zone
     */
    Optional<EstimatedFareDTO> fareOptional2 =
      fareService.estimateFare(outsideAirportLocation, center, cityCarType.getCarType(), Constants.City.AUSTIN.getId());

    if (!fareOptional2.isPresent()) {
      fail("Failed to estimate fare");
    }

    EstimatedFareDTO fare2 = fareOptional2.get();

    /*
     * Calculate target difference - Airport Fee with City Fee and Processing Fee on top of it
     */
    Optional<Airport> airport = airportService.getAirportForLocation(airportLocation);
    Money fareDiff = airport.map(Airport::getPickupFee).orElse(Constants.ZERO_USD);

    // visual check
    log.info("Fare 1: {}", fare1.getDefaultEstimate().getTotalFare());
    log.info("Fare 2: {}", fare2.getDefaultEstimate().getTotalFare());
    log.info("Fare diff actual: {}", fare1.getDefaultEstimate().getTotalFare().minus(fare2.getDefaultEstimate().getTotalFare()));
    log.info("Fare diff expected: {}", fareDiff);

    // check
    assertThat(fare1.getDefaultEstimate().getTotalFare().minus(fare2.getDefaultEstimate().getTotalFare()).getAmount().doubleValue(),
      is(closeTo(fareDiff.getAmount().doubleValue(), 2.6)));
  }

}
