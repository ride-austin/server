package com.rideaustin.activedrivers;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.maps.model.LatLng;
import com.rideaustin.model.user.Administrator;
import com.rideaustin.model.user.Driver;

import com.rideaustin.test.util.TestUtils;

@Category(ActiveDrivers.class)
public class ActiveDriversTestIT extends AbstractActiveDriverTest {

  @Test
  public void getClosestActiveDriversForAdmin() throws Exception {

    Administrator administrator = administratorFixture.getFixture();
    Driver driver = driverFixture.getFixture();
    driverAction.goOnline(driver.getEmail(), 30.202596, -97.667001);

    String content = administratorAction.getClosestActiveDriverForAdmin(administrator.getEmail(), new LatLng(30.202596, -97.667001), TestUtils.REGULAR);
    System.out.println(content);
    assertContentIsCorrect(content);
  }
}
