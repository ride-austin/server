package com.rideaustin.dispatch.womenonly;

import java.util.LinkedList;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.BaseWODispatchSetup;
import com.rideaustin.test.util.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractWomenOnlyDispatchTest<T extends BaseWODispatchSetup<T>> extends AbstractNonTxTests<T> {

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected DriverAction driverAction;

  @Inject
  protected JdbcTemplate jdbcTemplate;

  protected ActiveDriver woDriver;
  protected ActiveDriver woFPDriver;
  protected ActiveDriver regularDriver;
  protected Rider rider;

  protected LatLng AUSTIN_CENTER ;
  protected static final String CAR_TYPE = TestUtils.PREMIUM;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    this.setup = createSetup();
    AUSTIN_CENTER = locationProvider.getCenter();
    this.woDriver = setup.getWoDriver();
    this.woFPDriver = setup.getWoFPDriver();
    this.rider = setup.getRider();
    this.regularDriver = setup.getRegularDriver();
  }

  protected LinkedList<Long> getDispatchHistory(Long ride) {
    return new LinkedList<>(
      jdbcTemplate.query("select active_driver_id from ride_driver_dispatches where ride_id = ? order by id",
        new Object[]{ride}, (rs, rowNum) -> rs.getLong(1))
    );
  }
}
