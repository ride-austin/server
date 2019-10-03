package com.rideaustin.service.email;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.rideaustin.model.user.Driver;
import com.rideaustin.test.fixtures.providers.RideFixtureProvider;

public abstract class AbstractEarningsEmailTest extends AbstractEmailTest {

  @Inject
  protected RideFixtureProvider rideFixtureProvider;

  protected void triggerJob(Driver driver, Class jobClass) {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("reportDate", LocalDate.now().plusDays(1));
    dataMap.put("driverId", driver.getId());
    schedulerService.triggerJob(jobClass, dataMap);
  }
}
