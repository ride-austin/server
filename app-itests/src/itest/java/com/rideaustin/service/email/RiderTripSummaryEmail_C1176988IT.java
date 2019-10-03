package com.rideaustin.service.email;

import java.util.List;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.surgepricing.AreaGeometry;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.surgepricing.SurgeAreaCache;
import com.rideaustin.test.asserts.EmailAssert;
import com.rideaustin.test.setup.C1176988Setup;
import com.rideaustin.testrail.TestCases;

@Category(Email.class)
public class RiderTripSummaryEmail_C1176988IT extends AbstractTripSummaryEmailNonTxTest<C1176988Setup> {

  @Inject
  private SurgeAreaCache surgeAreaCache;

  @Test
  @TestCases("C1176988")
  public void shouldSendTripSummaryEmail_WithSurgeFactorAsGreaterThanOne() throws Exception {
    final Rider rider = setup.getRider();
    final ActiveDriver activeDriver = setup.getActiveDriver();
    surgeAreaCache.refreshCache(true);

    final AreaGeometry areaGeometry = setup.getSurgeArea().getAreaGeometry();
    final Long rideId = rideAction.performRide(rider, new LatLng(areaGeometry.getCenterPointLat(), areaGeometry.getCenterPointLng()),
      locationProvider.getRandomLocation(), activeDriver, true);

    paymentService.processRidePayment(rideId);

    List<InterceptingEmailService.Email> recentMessages = fetchEmailsWithSleep();
    EmailAssert.assertThat(recentMessages).tripSummaryEmailDeliveredWithPF(startDate, rider.getEmail());
  }

}
