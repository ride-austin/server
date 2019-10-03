package com.rideaustin.rideupgrade;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.enums.EventType;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideUpgradeRequest;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.MobileDriverRideDto;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.asserts.DriverRideAssert;
import com.rideaustin.test.asserts.EventAssertHelper;
import com.rideaustin.test.asserts.RideUpgradeAssert;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.RideFixture;
import com.rideaustin.test.fixtures.SessionFixture;
import com.rideaustin.test.stubs.NotificationFacade;
import com.rideaustin.test.util.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractRideUpgradeTest extends ITestProfileSupport {

  @Inject
  @Named("driverAssignedRide")
  protected RideFixture rideFixture;
  protected Ride ride;

  @Inject
  @Named("app320Session")
  private SessionFixture sessionFixture;

  @Inject
  protected DriverAction driverAction;
  @Inject
  protected RiderAction riderAction;
  @Inject
  protected EventAssertHelper eventAssertHelper;
  @Inject
  protected NotificationFacade notificationFacade;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    rideFixture.setRiderSessionFixture(sessionFixture);
    ride = rideFixture.getFixture();
  }

  protected void assertRequest(Driver driver, Rider rider, String source, RideUpgradeRequestStatus status, BigDecimal surgeFactor, long rideId) {
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

  protected void assertNotificationPushedToRider(Rider rider) {
    assertEquals(notificationFacade.getUser(), rider.getUser());
    assertEquals(notificationFacade.getDataMap().get("eventKey"), "RIDE_UPGRADE");
  }

  protected void cancelRequest(Driver driver, Rider rider, String source) throws Exception {
    driverAction
      .cancelRideUpgrade(driver.getEmail())
      .andExpect(status().isOk())
      .andExpect(content().json("{\"message\":\"Your request has been cancelled\"}"));

    assertRequest(driver, rider, source, RideUpgradeRequestStatus.CANCELLED, Constants.NEUTRAL_SURGE_FACTOR, ride.getId());
  }

  protected void issueRequest(Driver driver, Rider rider, String source, long rideId) throws Exception {
    driverAction
      .requestRideUpgrade(driver.getEmail(), TestUtils.SUV)
      .andExpect(status().isOk())
      .andExpect(content().json("{\"message\":\"Your request has been submitted to rider\"}"));

    assertRequest(driver, rider, source, RideUpgradeRequestStatus.REQUESTED, Constants.NEUTRAL_SURGE_FACTOR, rideId);
    assertNotificationPushedToRider(rider);
  }

  protected void acceptRequest(Driver driver, Rider rider, String source, long rideId) throws Exception {
    riderAction
      .acceptRideUpgrade(rider.getEmail())
      .andExpect(status().isOk())
      .andExpect(content().json("{\"message\":\"You have accepted ride upgrade request\"}"));

    eventAssertHelper.assertLastEventIsSent(driver, EventType.RIDE_UPGRADE_ACCEPTED);
    assertRequest(driver, rider, source, RideUpgradeRequestStatus.ACCEPTED, Constants.NEUTRAL_SURGE_FACTOR, rideId);
  }

  protected void assertRideCategoryAndStatus(String driverEmail, String category, RideUpgradeRequestStatus status) throws Exception {
    MobileDriverRideDto rideInfo = driverAction
      .getRideInfo(driverEmail, ride.getId());

    DriverRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(category)
      .hasUpgradeRequestStatus(status);
  }

  protected void declineRequest(Driver driver, Rider rider, String source) throws Exception {
    riderAction
      .declineRideUpgrade(rider.getEmail())
      .andExpect(status().isOk());

    eventAssertHelper.assertLastEventIsSent(driver, EventType.RIDE_UPGRADE_DECLINED);
    assertRequest(driver, rider, source, RideUpgradeRequestStatus.DECLINED, Constants.NEUTRAL_SURGE_FACTOR, ride.getId());
  }
}
