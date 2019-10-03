package com.rideaustin.redispatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182969
 */
@Category(Redispatch.class)
public class C1182969IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Test
  @TestCases("C1182969")
  public void testWithDestination() throws Exception {
    doTestRedispatch(locationProvider.getAirportLocation());
  }

}
