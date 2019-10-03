package com.rideaustin.applepay;

import static org.junit.Assert.assertNotNull;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.test.setup.DefaultApplePaySetup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

/**
 * Apple Pay - Rider should be pre-charged with $1
 */
@Category(ApplePay.class)
public class C1181738PreChargeCheckIT extends AbstractApplePayTest<DefaultApplePaySetup> {

  @Inject
  private RideDslRepository repository;

  @Test
  @TestCases("C1181738")
  public void test() throws Exception {
    Long rideId = riderAction.requestRide(rider.getEmail(), PICKUP_LOCATION,
      TestUtils.REGULAR, null, SAMPLE_TOKEN);

    assertNotNull(repository.findOne(rideId).getPreChargeId());

    awaitStatus(rideId, RideStatus.NO_AVAILABLE_DRIVER);
  }
}
