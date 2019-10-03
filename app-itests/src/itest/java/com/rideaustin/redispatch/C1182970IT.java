package com.rideaustin.redispatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.test.setup.DefaultRedispatchTestSetup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182970
 */
@Category(Redispatch.class)
public class C1182970IT extends AbstractRedispatchTest<DefaultRedispatchTestSetup> {

  @Test
  @TestCases("C1182970")
  public void testWithoutDestination() throws Exception {
    doTestRedispatch(null);
  }

}
