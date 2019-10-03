package com.rideaustin.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;

import com.rideaustin.service.DefaultSearchDriverHandler.IgnoredDriverFilter;
import com.rideaustin.service.model.OnlineDriverDto;

public class IgnoredDriverFilterTest {

  private IgnoredDriverFilter testedInstance;

  @Test
  public void filterReturnsTrueOnNullIgnoredList() {
    testedInstance = new IgnoredDriverFilter(new ActiveDriverSearchCriteria(
      34.68198, -97.18919, null, 0, "REGULAR", 1, 1L, null, 0, 0
    ));

    final boolean result = testedInstance.filter(new OnlineDriverDto());

    assertTrue(result);
  }

  @Test
  public void filterReturnsFalseOnIgnoredListContainingDriverId() {
    final OnlineDriverDto driver = new OnlineDriverDto();
    testedInstance = new IgnoredDriverFilter(new ActiveDriverSearchCriteria(
      34.68198, -97.18919, Collections.singletonList(driver.getId()), 0, "REGULAR", 1, 1L, null, 0, 0
    ));

    final boolean result = testedInstance.filter(driver);

    assertFalse(result);
  }

  @Test
  public void filterReturnsTrueOnIgnoredListNotContainingDriverId() {
    final OnlineDriverDto driver = new OnlineDriverDto();
    testedInstance = new IgnoredDriverFilter(new ActiveDriverSearchCriteria(
      34.68198, -97.18919, Collections.emptyList(), 0, "REGULAR", 1, 1L, null, 0, 0
    ));

    final boolean result = testedInstance.filter(driver);

    assertTrue(result);
  }

}
