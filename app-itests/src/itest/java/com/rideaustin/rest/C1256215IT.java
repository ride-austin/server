package com.rideaustin.rest;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.rideaustin.test.util.TestUtils.unwrapProxy;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.google.maps.model.LatLng;
import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.service.ActiveDriversService;
import com.rideaustin.test.actions.DriverAction;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.testrail.TestCases;

@ActiveProfiles({"dev","itest"})
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class C1256215IT extends AbstractTransactionalJUnit4SpringContextTests {

  private LatLng location = new LatLng(30.269372, -97.740394);

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  private DriverAction driverAction;

  @Inject
  private ActiveDriversService activeDriversService;

  @Inject
  private ActiveDrivers activeDrivers;

  private ActiveDriver activeDriver;

  @Before
  public void before() throws Exception {
    activeDriver = activeDriverFixtureProvider.create().getFixture();
    //restore service after setting to mock in other tests
    ReflectionTestUtils.setField(unwrapProxy(activeDrivers), "activeDriversService", activeDriversService);
  }

  @Test
  @TestCases("C1256215")
  public void test_Driver_sent_location_update_after_server_sent_driver_offline() throws Exception {


    driverAction.locationUpdate(activeDriver, location.lat, location.lng)
      .andDo(print())
      .andExpect(status().isOk())
    ;

    activeDriversService.deactivateAsAdmin(activeDriver.getId());

    driverAction.locationUpdate(activeDriver, location.lat, location.lng)
      .andDo(print())
      .andExpect(status().isConflict())
      .andExpect(content().string(containsString("Driver is inactive")))
    ;
  }

}
