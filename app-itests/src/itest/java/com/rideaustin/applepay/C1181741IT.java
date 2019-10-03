package com.rideaustin.applepay;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.model.States;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(ApplePay.class)
public class C1181741IT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Inject
  private RideDslRepository repository;

  @Test
  @TestCases("C1181741")
  public void test() throws Exception {

    Long rideId = riderAction.requestRide(rider.getEmail(), PICKUP_LOCATION, TestUtils.REGULAR, null, SAMPLE_TOKEN);

    awaitState(rideId, States.NO_AVAILABLE_DRIVER);

    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), rideId);
    RiderRideAssert.assertThat(rideInfo)
      .hasStatus(RideStatus.NO_AVAILABLE_DRIVER);

    assertNotNull(repository.findOne(rideId).getPreChargeId());
  }

}
