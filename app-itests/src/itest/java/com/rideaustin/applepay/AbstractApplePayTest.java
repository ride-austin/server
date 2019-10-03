package com.rideaustin.applepay;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.Constants;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.thirdparty.StripeServiceMockImpl;
import com.rideaustin.test.AbstractNonTxTests;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RideAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.config.TestSetupConfig;
import com.rideaustin.test.setup.BaseApplePaySetup;
import com.rideaustin.test.stubs.transactional.PaymentService;
import com.rideaustin.test.util.TestUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class, TestSetupConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public abstract class AbstractApplePayTest<T extends BaseApplePaySetup<T>> extends AbstractNonTxTests<T> {

  protected static final String SAMPLE_TOKEN = "sample_token";
  protected static final LatLng PICKUP_LOCATION = new LatLng(30.202596, -97.667001);
  protected LatLng defaultLocation = new LatLng(30.269372, -97.740394);

  @Inject
  protected PaymentService paymentService;

  @Inject
  protected StripeServiceMockImpl stripeServiceMock;

  @Inject
  protected RiderAction riderAction;

  @Inject
  protected DriverAction driverAction;
  @Inject
  private RideAction rideAction;
  @Inject
  protected JdbcTemplate jdbcTemplate;

  protected Rider rider;

  @Before
  public void setUp() throws Exception {
    super.setUp();
    setup = createSetup();
    rider = setup.getRider();
  }

  public void assertNumberOfActiveDriver(int number) {
    List<Map<String, Object>> ad = jdbcTemplate.queryForList("SELECT * FROM active_drivers");
    assertThat(ad.size(), is(equalTo(number)));
  }

  protected Long performRide(LatLng pickupLocation, LatLng dropoffLocation, ActiveDriver driver, String applePayToken) throws Exception {
    return rideAction.performRide(getRider(), pickupLocation, dropoffLocation, driver, applePayToken, TestUtils.REGULAR, 1000, Constants.DEFAULT_CITY_ID, false);
  }

  protected Rider getRider() {
    return rider;
  }
}
