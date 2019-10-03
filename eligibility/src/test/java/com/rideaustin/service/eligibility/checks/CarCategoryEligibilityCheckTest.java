package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.ride.Car;
import com.rideaustin.service.eligibility.DriverEligibilityCheckContext;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.user.CarTypesCache;

public class CarCategoryEligibilityCheckTest {

  private static final Set<String> CAR_CATEGORIES = ImmutableSet.of("REGULAR", "SUV");
  @Mock
  private CarTypesCache carTypesCache;
  @Mock
  private Map<String, Object> context;
  @InjectMocks
  private CarCategoryEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance.setCarTypesCache(carTypesCache);
    when(context.get(eq(DriverEligibilityCheckContext.CAR_CATEGORIES))).thenReturn(CAR_CATEGORIES);
    when(carTypesCache.fromBitMask(eq(1))).thenReturn(Collections.singleton("REGULAR"));
    when(carTypesCache.fromBitMask(eq(5))).thenReturn(CAR_CATEGORIES);
  }

  @Test
  public void testCheckReturnNoErrorIfCarCategoryIsContainedInRequested() throws Exception {
    Car car = new Car();
    car.setCarCategoriesBitmask(5);

    Optional<EligibilityCheckError> result = testedInstance.check(car);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsErrorIfCarIsNull() {
    Optional<EligibilityCheckError> result = testedInstance.check(null);

    assertTrue(result.isPresent());
    assertEquals(CarCategoryEligibilityCheck.MESSAGE+CAR_CATEGORIES, result.get().getMessage());
  }

  @Test
  public void testCheckReturnsErrorIfCarCategoryDoesntMatchRequested() {
    Car car = new Car();
    car.setCarCategoriesBitmask(1);

    Optional<EligibilityCheckError> result = testedInstance.check(car);

    assertTrue(result.isPresent());
    assertEquals(CarCategoryEligibilityCheck.MESSAGE+CAR_CATEGORIES, result.get().getMessage());
  }

}