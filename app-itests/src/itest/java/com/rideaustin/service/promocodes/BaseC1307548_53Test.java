package com.rideaustin.service.promocodes;

import javax.inject.Inject;

import org.junit.Before;
import org.springframework.jdbc.core.JdbcTemplate;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.payment.PaymentService;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.setup.BaseC1307548_53Setup;

public abstract class BaseC1307548_53Test<T extends BaseC1307548_53Setup<T>> extends AbstractNonTxPromocodeTest<T> {

  protected Promocode promocode;
  protected Rider rider;
  protected ActiveDriver activeDriver;

  @Inject
  protected RideAction rideAction;

  @Inject
  protected PaymentService paymentService;

  @Inject
  protected CarTypesCache carTypesCache;
  @Inject
  protected JdbcTemplate jdbcTemplate;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    this.promocode = setup.getPromocode();
    this.rider = setup.getRider();
    this.activeDriver = setup.getActiveDriver();
    execute(() -> jdbcTemplate.update("update city_car_types SET minimum_fare = 0.01"));
    carTypesCache.refreshCache();
  }
}
