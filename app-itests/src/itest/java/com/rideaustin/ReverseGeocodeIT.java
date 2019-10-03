package com.rideaustin;

import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.Address;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.service.MapService;
import com.rideaustin.test.config.FixtureConfig;
import com.rideaustin.test.config.ITestProfile;
import com.rideaustin.test.config.TestActionsConfig;

@Ignore
@ITestProfile
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class, FixtureConfig.class, TestActionsConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class ReverseGeocodeIT {

  @Inject
  private MapService mapService;

  @Test
  public void testReversePartial() throws RideAustinException {
    final Address address = mapService.reverseGeocodeAddress(30.151951, -97.810674);
    System.out.println(address.getAddress());
  }
}
