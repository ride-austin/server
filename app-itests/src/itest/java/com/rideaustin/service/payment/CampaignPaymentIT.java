package com.rideaustin.service.payment;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.RideTrackerService;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.CampaignPaymentSetup;
import com.rideaustin.test.stubs.transactional.PaymentService;

@ITestProfile
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
public class CampaignPaymentIT extends AbstractNonTxTests<CampaignPaymentSetup> {

  @Inject
  private PaymentService paymentService;
  @Inject
  private RideDslRepository rideDslRepository;
  @Inject
  private RideTrackerService rideTrackerService;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
  }

  @Test
  public void test() throws RideAustinException {
    Ride ride = setup.getRide();
    final RideTracker last = rideTrackerService.endRide(ride.getId(), new RideTracker(ride.getEndLocationLat(), ride.getEndLocationLong(),
      null, null, null, Long.MAX_VALUE));
    ride.setDistanceTravelled(last.getDistanceTravelled());
    rideDslRepository.save(ride);
    paymentService.processRidePayment(ride);

    ride = rideDslRepository.findOne(ride.getId());

//    assertEquals(Constants.ZERO_USD, ride.getProcessingFee());
    System.out.println(ride.getFareDetails());
  }
}
