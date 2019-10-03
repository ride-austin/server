package com.rideaustin.redispatch;

import java.math.BigDecimal;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.rideaustin.test.setup.C1182974Setup;
import com.rideaustin.testrail.TestCases;

/**
 * https://testrail.devfactory.com/index.php?/cases/view/1182974
 * https://testrail.devfactory.com/index.php?/cases/view/1182976
 */
@Category({Redispatch.class})
public class C1182974IT extends AbstractSurgeRedispatchTest<C1182974Setup> {

  @Test
  @TestCases({"C1182974", "C1182976"})
  public void test() throws Exception {
    doTestRedispatch(null);
  }

  protected BigDecimal getExpectedSurgeFare() {
    return BigDecimal.ZERO;
  }

  @Override
  protected BigDecimal getNewFactor() {
    return FACTOR;
  }

}
