package com.rideaustin.redispatch;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.ImmutableSet;
import com.google.maps.model.LatLng;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.FarePaymentDto;
import com.rideaustin.rest.model.MobileRiderRideDto;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.test.asserts.FarePaymentAssert;
import com.rideaustin.test.asserts.RiderRideAssert;
import com.rideaustin.test.setup.C1182978Setup;
import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182978
 */
@Category(Redispatch.class)
public class C1182978IT extends AbstractRedispatchTest<C1182978Setup> {

  private Rider secondRider;
  private Rider thirdRider;

  @Inject
  private PaymentService paymentService;

  private LatLng endLocation;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.secondRider = this.setup.getSecondRider();
    this.thirdRider = this.setup.getThirdRider();
    endLocation = locationProvider.getAirportLocation();
  }

  @Test
  @TestCases("C1182978")
  public void test() throws Exception {
    doTestRedispatch(endLocation);
  }

  @Override
  protected Long requestRide(LatLng destination, LatLng riderLocation) throws Exception {
    Long ride = super.requestRide(destination, riderLocation);
    Long splitFare = riderAction.requestSplitFare(rider.getEmail(), ride, ImmutableSet.of(secondRider.getPhoneNumber()));
    riderAction.acceptSplitFare(secondRider.getEmail(), splitFare)
      .andExpect(status().isOk());
    riderAction.requestSplitFare(rider.getEmail(), ride, ImmutableSet.of(thirdRider.getPhoneNumber()));
    return ride;
  }

  @Override
  protected void acceptRedispatched(ActiveDriver driver, Long ride) throws Exception {
    super.acceptRedispatched(driver, ride);
    driverAction.reach(driver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    driverAction.startRide(driver.getDriver().getEmail(), ride)
      .andExpect(status().isOk());
    driverAction.endRide(driver.getDriver().getEmail(), ride, endLocation.lat, endLocation.lng)
      .andExpect(status().isOk());
    awaitStatus(ride, RideStatus.COMPLETED);
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    MobileRiderRideDto rideInfo = riderAction.getRideInfo(rider.getEmail(), ride);

    RiderRideAssert.assertThat(rideInfo)
      .hasRequestedCategory(TestUtils.REGULAR)
      .hasDriverAssigned(secondDriver.getId())
      .hasStartLocation(riderLocation)
      .hasEndLocation(destination);

    forceEndRide(ride);
    paymentService.processRidePayment(ride);

    List<FarePaymentDto> farePaymentsInfo = riderAction.requestFarePaymentsInfo(rider.getEmail(), ride);
    assertThat(farePaymentsInfo.size(), is(2));
    Optional<FarePaymentDto> mainPayment = farePaymentsInfo.stream().filter(FarePaymentDto::isMainRider).findFirst();
    Optional<FarePaymentDto> secondaryPayment = farePaymentsInfo.stream().filter(fp -> !fp.isMainRider()).findFirst();

    assertTrue("Main payment is expected to be present", mainPayment.isPresent());
    assertTrue("Secondary payment is expected to be present", secondaryPayment.isPresent());

    FarePaymentAssert.assertThat(mainPayment.get())
      .isAccepted()
      .hasCharge(7.0);

    FarePaymentAssert.assertThat(secondaryPayment.get())
      .isAccepted()
      .hasCharge(7.0);
  }
}
