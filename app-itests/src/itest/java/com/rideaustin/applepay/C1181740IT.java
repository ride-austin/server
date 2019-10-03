package com.rideaustin.applepay;

import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.model.States;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(ApplePay.class)
public class C1181740IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Inject
  private RideDslRepository repository;

  private ActiveDriver activeDriver;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    activeDriver = setup.getActiveDriver();
  }

  @Test
  @TestCases("C1181740")
  public void test() throws Exception {
    driverAction.goOnline(activeDriver.getDriver().getEmail(), defaultLocation)
      .andExpect(status().isOk());

    Long rideId = riderAction.requestRide(rider.getEmail(), PICKUP_LOCATION,
      TestUtils.REGULAR, null, SAMPLE_TOKEN);

    awaitDispatch(activeDriver, rideId);

    riderAction.cancelRide(rider.getEmail(), rideId)
      .andExpect(status().isOk());

    awaitState(rideId, States.RIDER_CANCELLED, States.ENDED);
    awaitStatus(rideId, RideStatus.RIDER_CANCELLED);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), rideId);
    RiderRideAssert.assertThat(rideInfo)
      .hasStatus(RideStatus.RIDER_CANCELLED);

    assertNotNull(repository.findOne(rideId).getPreChargeId());
  }
}
