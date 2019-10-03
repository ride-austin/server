package com.rideaustin.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import com.rideaustin.service.DefaultSearchDriverHandler.CarTypeDriverFilter;
import com.rideaustin.service.model.OnlineDriverDto;

public class CarTypeDriverFilterTest {

  private CarTypeDriverFilter testedInstance;

  @Test
  public void filterReturnsTrueForPresentCarTypeBitmask() {
    testedInstance = new CarTypeDriverFilter(new ActiveDriverSearchCriteria(
      34.68198, -97.18919, new ArrayList<>(), 0, "REGULAR", 1, 1L, null, 0, 0
    ));
    final OnlineDriverDto driver = new OnlineDriverDto();
    driver.setAvailableCarCategoriesBitmask(3);

    final boolean result = testedInstance.filter(driver);

    assertTrue(result);
  }

  @Test
  public void filterReturnsFalseForAbsentCarTypeBitmask() {
    testedInstance = new CarTypeDriverFilter(new ActiveDriverSearchCriteria(
      34.68198, -97.18919, new ArrayList<>(), 0, "REGULAR", 1, 1L, null, 0, 0
    ));
    final OnlineDriverDto driver = new OnlineDriverDto();
    driver.setAvailableCarCategoriesBitmask(2);

    final boolean result = testedInstance.filter(driver);

    assertFalse(result);
  }

}
