package com.rideaustin.redispatch;

import java.math.BigDecimal;

import javax.inject.Inject;

import org.junit.Before;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.surgepricing.SurgeArea;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.service.surgepricing.SurgeAreaCache;
import com.rideaustin.test.actions.AdministratorAction;
import com.rideaustin.test.setup.BaseSurgeRedispatchTestSetup;

public abstract class AbstractSurgeRedispatchTest<T extends BaseSurgeRedispatchTestSetup<T>> extends AbstractRedispatchTest<T> {

  protected static final BigDecimal FACTOR = BigDecimal.valueOf(2.0);

  protected Administrator administrator;

  @Inject
  protected AdministratorAction administratorAction;

  @Inject
  protected PaymentService paymentService;

  protected SurgeArea surgeArea;

  @Inject
  private SurgeAreaCache surgeAreaCache;

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    administrator = this.setup.getAdministrator();
    surgeArea = this.setup.getSurgeArea();
    surgeAreaCache.refreshCache(true);
  }

  @Override
  protected Long requestAndAccept(LatLng destination, ActiveDriver firstDriver, LatLng riderLocation) throws Exception {
    Long ride = super.requestAndAccept(destination, firstDriver, riderLocation);

    administratorAction.updateSurgeFactor(administrator.getEmail(), surgeArea, getNewFactor());
    return ride;
  }

  @Override
  protected void assertRideRedispatched(LatLng destination, ActiveDriver secondDriver, LatLng riderLocation, LatLng secondDriverLocation, Long ride) throws Exception {
    super.assertRideRedispatched(destination, secondDriver, riderLocation, secondDriverLocation, ride);

    forceEndRide(ride);

    execute(() -> {
      try {
        paymentService.processRidePayment(ride);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    });
  }

  protected abstract BigDecimal getNewFactor();

  protected abstract BigDecimal getExpectedSurgeFare();

}
