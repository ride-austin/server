package com.rideaustin.rideupgrade;

import java.time.Instant;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.env.Environment;

import com.jayway.awaitility.Awaitility;
import com.rideaustin.model.enums.RideUpgradeRequestStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.ride.RideUpgradeService;
import com.rideaustin.testrail.TestCases;

public class C1226713IT extends AbstractRideUpgradeTest {

  @Inject
  private Environment environment;
  @Inject
  private RideUpgradeService service;

  private long expirationTimeout;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    expirationTimeout = environment.getProperty("ride.upgrade.request.expiration.timeout", Integer.class, 45) + 1;
  }

  @Test
  @TestCases("C1226713")
  public void test() throws Exception {
    Driver driver = ride.getActiveDriver().getDriver();
    Rider rider = ride.getRider();
    String source = ride.getRequestedCarType().getCarCategory();
    Instant start = Instant.now();

    issueRequest(driver, rider, source, ride.getId());

    Awaitility.await().forever().until(() ->
      Instant.now().getEpochSecond() - start.getEpochSecond() > expirationTimeout
    );

    service.expireRequests();

    declineRequest(driver, rider, source);
    assertRideCategoryAndStatus(driver.getEmail(), source, RideUpgradeRequestStatus.DECLINED);
  }
}
