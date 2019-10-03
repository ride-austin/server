package com.rideaustin.queue;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.inject.Inject;
import javax.inject.Named;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.test.response.AreaQueuePositions;
import com.rideaustin.test.asserts.DriverQueueAssert;
import com.rideaustin.test.fixtures.CarFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;
import com.rideaustin.test.fixtures.providers.DriverFixtureProvider;

import com.rideaustin.test.util.TestUtils;
import com.rideaustin.testrail.TestCases;

@Category(AirportQueue.class)
public class C1183010IT extends AbstractAirportQueueTest {

  @Inject
  private ActiveDriverFixtureProvider provider;

  @Inject
  private DriverFixtureProvider driverFixtureProvider;

  @Inject
  @Named("regularCar")
  private CarFixture regularCar;

  @Inject
  @Named("suvCar")
  private CarFixture suvCar;

  @Inject
  @Named("premiumCar")
  private CarFixture premiumCar;

  @Test
  @TestCases("C1183010")
  public void test() throws Exception {
    ActiveDriver regularDriver = provider.create(driverFixtureProvider.create(regularCar)).getFixture();
    ActiveDriver suvDriver = provider.create(driverFixtureProvider.create(suvCar)).getFixture();
    ActiveDriver premiumDriver = provider.create(driverFixtureProvider.create(premiumCar)).getFixture();

    appearInQueue(regularDriver, new String[]{TestUtils.REGULAR});
    AreaQueuePositions regularPosition = driverAction
      .getQueuePosition(regularDriver.getDriver());
    DriverQueueAssert.assertThat(regularPosition)
      .hasLength(TestUtils.REGULAR, 1)
      .hasPosition(TestUtils.REGULAR, 0);

    appearInQueue(suvDriver, new String[]{TestUtils.REGULAR, TestUtils.SUV});
    regularPosition = driverAction
      .getQueuePosition(regularDriver.getDriver());
    DriverQueueAssert.assertThat(regularPosition)
      .hasLength(TestUtils.REGULAR, 2)
      .hasPosition(TestUtils.REGULAR, 0);
    AreaQueuePositions suvPosition = driverAction
      .getQueuePosition(suvDriver.getDriver());
    DriverQueueAssert.assertThat(suvPosition)
      .hasLength(TestUtils.REGULAR, 2)
      .hasLength(TestUtils.SUV, 1)
      .hasPosition(TestUtils.REGULAR, 1)
      .hasPosition(TestUtils.SUV, 0);

    appearInQueue(premiumDriver, new String[]{TestUtils.REGULAR, TestUtils.PREMIUM});
    regularPosition = driverAction
      .getQueuePosition(regularDriver.getDriver());
    DriverQueueAssert.assertThat(regularPosition)
      .hasLength(TestUtils.REGULAR, 3)
      .hasPosition(TestUtils.REGULAR, 0);
    suvPosition = driverAction
      .getQueuePosition(suvDriver.getDriver());
    DriverQueueAssert.assertThat(suvPosition)
      .hasLength(TestUtils.REGULAR, 3)
      .hasLength(TestUtils.SUV, 1)
      .hasPosition(TestUtils.REGULAR, 1)
      .hasPosition(TestUtils.SUV, 0);
    AreaQueuePositions premiumPosition = driverAction
      .getQueuePosition(premiumDriver.getDriver());
    DriverQueueAssert.assertThat(premiumPosition)
      .hasLength(TestUtils.REGULAR, 3)
      .hasLength(TestUtils.PREMIUM, 1)
      .hasPosition(TestUtils.REGULAR, 2)
      .hasPosition(TestUtils.PREMIUM, 0);
  }

  private void appearInQueue(ActiveDriver driver, String[] categories) throws Exception {
    driverAction
      .locationUpdate(driver, airportLocation.lat, airportLocation.lng, categories)
      .andExpect(status().isOk());
    updateQueue();
    sleep(500);
  }

}
