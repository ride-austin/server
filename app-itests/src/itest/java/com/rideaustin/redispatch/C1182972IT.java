package com.rideaustin.redispatch;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.dispatch.womenonly.WomenOnly;
import com.rideaustin.test.setup.C1182972Setup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182972
 */
@Category({Redispatch.class, WomenOnly.class})
public class C1182972IT extends AbstractRedispatchTest<C1182972Setup> {

  @Test
  @TestCases("C1182972")
  public void test() throws Exception {
    doTestRedispatch(null);
  }

}
