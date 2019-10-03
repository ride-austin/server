package com.rideaustin.dispatch;

import java.util.LinkedList;

import org.junit.Before;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.setup.C1177112Setup;

public abstract class BaseC1177112Test extends BaseDispatchTest<C1177112Setup> {

  protected Rider rider;
  protected ActiveDriver driver;

  protected static final LatLng AUSTIN_CENTER = new LatLng(30.2747789, -97.7384711);

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.rider = setup.getRider();
  }

  protected LinkedList<Long> getDispatchHistory(Long ride) {
    return new LinkedList<>(
      JDBC_TEMPLATE.query("select active_driver_id from ride_driver_dispatches where ride_id = ? order by id",
        new Object[]{ride}, (rs, rowNum) -> rs.getLong(1))
    );
  }
}
