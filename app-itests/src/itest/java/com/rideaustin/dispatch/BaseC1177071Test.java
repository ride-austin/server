package com.rideaustin.dispatch;

import org.junit.Before;

import com.google.maps.model.LatLng;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.setup.DefaultRedispatchTestSetup;

public abstract class BaseC1177071Test extends BaseDispatchTest<DefaultRedispatchTestSetup> {

  protected LatLng defaultClosestLocation = new LatLng(30.269372, -97.740394);
  protected LatLng defaultNotClosestLocation = new LatLng(30.262372, -97.744394);

  protected Rider rider;
  protected ActiveDriver firstActiveDriver;
  protected ActiveDriver secondActiveDriver;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    rider = this.setup.getRider();
    firstActiveDriver = this.setup.getFirstActiveDriver();
    secondActiveDriver = this.setup.getSecondActiveDriver();
  }
}
