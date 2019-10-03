package com.rideaustin.payment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.airports.Airport;
import com.rideaustin.service.airport.AirportService;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.setup.C1177100Setup;

@Category(Payment.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
@ITestProfile
public class RA12078AirportDropoffFeeIT extends AbstractNonTxTests<C1177100Setup> {

  @Inject
  private AirportService airportService;
  @Inject
  private PaymentService paymentService;
  @Inject
  private RideAction rideAction;

  private LatLng airportLocation;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    airportLocation = locationProvider.getAirportLocation();
    configurationItemCache.setBooleanConfig(1L, ClientType.CONSOLE, "rideMessaging", "enabled", true);
  }

  @Test
  public void test() throws Exception {
    final Long rideId = rideAction.performRide(setup.getRider(), locationProvider.getRandomLocation(), locationProvider.getAirportLocation(), setup.getActiveDriver());

    paymentService.processRidePayment(rideId);

    Optional<Airport> airport = airportService.getAirportForLocation(airportLocation);
    if (!airport.isPresent()) {
      fail("Expected to end ride in airport");
    }

    assertEquals(rideDslRepository.findOne(rideId).getAirportFee(), airport.get().getDropoffFee());
  }
}
