package com.rideaustin;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Rider;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.actions.RiderAction;
import com.rideaustin.test.common.ITestProfileSupport;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.TestActionsConfig;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.RiderFixtureProvider;
import com.rideaustin.test.util.TestUtils;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class LoadIT extends ITestProfileSupport {

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;
  @Inject
  private RiderFixtureProvider riderFixtureProvider;

  @Inject
  private DriverAction driverAction;
  @Inject
  private RiderAction riderAction;

  private List<ActiveDriver> drivers = new ArrayList<>();
  private List<Rider> riders = new ArrayList<>();

  @Test
  public void test() throws Exception {
    for (int i = 0; i < 200; i++) {
      drivers.add(activeDriverFixtureProvider.create().getFixture());

      if (i % 20 == 0) {
        System.out.println(i + " drivers created");
      }
    }
    for (int i = 0; i < 400; i++) {
      riders.add(riderFixtureProvider.create().getFixture());
      if (i % 40 == 0) {
        System.out.println(i + " riders created");
      }
    }

    for (int i = 0; i < 200; i++) {
      LatLng randomLocation = locationProvider.getRandomLocation();
      driverAction.goOnline(drivers.get(i).getDriver().getEmail(), randomLocation.lat, randomLocation.lng);
    }

    for (int i = 0; i < 400; i++) {
      LatLng randomLocation = locationProvider.getRandomLocation();
      riderAction.searchDrivers(riders.get(i).getEmail(), randomLocation, TestUtils.REGULAR, null);
    }
  }
}
