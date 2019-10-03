package com.rideaustin.rideupgrade;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.dispatch.womenonly.WomenOnly;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.LocationProvider;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.actions.cascaded.CascadedDriverAction;
import com.rideaustin.test.asserts.EventAssertHelper;
import com.rideaustin.test.asserts.RideUpgradeAssert;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.C1266357Setup;
import com.rideaustin.test.stubs.NotificationFacade;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category({WomenOnly.class, RideUpgrade.class})
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1266357IT extends AbstractNonTxTests<C1266357Setup> {

  @Inject
  private RiderAction riderAction;
  @Inject
  private DriverAction driverAction;
  @Inject
  private LocationProvider locationProvider;
  @Inject
  protected EventAssertHelper eventAssertHelper;
  @Inject
  protected NotificationFacade notificationFacade;

  @Inject
  private RideDslRepository repository;
  @Inject
  private JdbcTemplate jdbcTemplate;

  @Inject
  private C1266357Setup setup;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = setup.setUp();
  }

  @Test
  @TestCases("C1266357")
  public void test() throws Exception {
    LatLng pickupLocation = locationProvider.getCenter();
    ActiveDriver activeDriver = setup.getActiveDriver();
    Driver driver = activeDriver.getDriver();
    driverAction.goOnline(driver.getEmail(), pickupLocation)
      .andExpect(status().isOk());

    Rider rider = setup.getRider();
    Long ride = riderAction.requestRide(rider.getEmail(), pickupLocation, TestUtils.REGULAR, "WOMEN_ONLY");
    Ride dbRide = repository.findOne(ride);
    dbRide.setRiderSession(setup.getRiderSession());
    repository.save(dbRide);

    awaitDispatch(activeDriver, ride);

    driverAction.acceptRide(activeDriver, ride);

    issueRequest(driver, rider, TestUtils.REGULAR, ride);
    acceptRequest(driver, rider, TestUtils.REGULAR, ride);

    CascadedDriverAction cascadedDriverAction = new CascadedDriverAction(driver, driverAction).withRideId(ride);

    cascadedDriverAction
      .reach()
      .startRide()
      .endRide(locationProvider.getOutsideAirportLocation().lat, locationProvider.getOutsideAirportLocation().lng);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.SUV);
  }

  protected void issueRequest(Driver driver, Rider rider, String source, long rideId) throws Exception {
    driverAction
      .requestRideUpgrade(driver.getEmail(), TestUtils.SUV)
      .andExpect(status().isOk())
      .andExpect(content().json("{\"message\":\"Your request has been submitted to rider\"}"));

    assertRequest(driver, rider, source, RideUpgradeRequestStatus.REQUESTED, Constants.NEUTRAL_SURGE_FACTOR, rideId);
    assertNotificationPushedToRider(rider);
  }

  private void acceptRequest(Driver driver, Rider rider, String source, long rideId) throws Exception {
    riderAction
      .acceptRideUpgrade(rider.getEmail())
      .andExpect(status().isOk())
      .andExpect(content().json("{\"message\":\"You have accepted ride upgrade request\"}"));

    eventAssertHelper.assertLastEventIsSent(driver, EventType.RIDE_UPGRADE_ACCEPTED);
    assertRequest(driver, rider, source, RideUpgradeRequestStatus.ACCEPTED, Constants.NEUTRAL_SURGE_FACTOR, rideId);
  }

  private void assertRequest(Driver driver, Rider rider, String source, RideUpgradeRequestStatus status, BigDecimal surgeFactor, long rideId) {
    RideUpgradeAssert.assertThat(getRequest())
      .belongsToRide(rideId)
      .isRequestedBy(driver.getId())
      .isRequestedFrom(rider.getId())
      .hasSource(source)
      .hasTarget(TestUtils.SUV)
      .hasStatus(status)
      .hasSurgeFactor(surgeFactor);
  }

  protected RideUpgradeRequest getRequest() {
    return jdbcTemplate.queryForObject("select * from ride_upgrade_requests limit 1", (rs, rowNum) -> {
      RideUpgradeRequest request = RideUpgradeRequest.builder()
        .source(rs.getString("source"))
        .target(rs.getString("target"))
        .requestedBy(rs.getLong("requested_by"))
        .requestedFrom(rs.getLong("requested_from"))
        .status(RideUpgradeRequestStatus.valueOf(rs.getString("status")))
        .rideId(rs.getLong("ride_id"))
        .surgeFactor(rs.getBigDecimal("surge_factor"))
        .expiresOn(rs.getDate("expires_on"))
        .build();
      request.setCreatedDate(rs.getDate("created_date"));
      return request;
    });
  }

  private void assertNotificationPushedToRider(Rider rider) {
    assertEquals(notificationFacade.getUser(), rider.getUser());
    assertEquals(notificationFacade.getDataMap().get("eventKey"), "RIDE_UPGRADE");
  }
}
