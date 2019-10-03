package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anySet;
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
import com.rideaustin.model.ride.ActiveDriver;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.eligibility.DriverEligibilityCheckContext;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import com.rideaustin.service.ride.DriverTypeService;
import com.rideaustin.service.user.DriverTypeCache;

public class DriverTypeEligibilityCheckTest {

  private static final Set<String> WOMEN_ONLY = Collections.singleton("WOMEN_ONLY");
  private static final ImmutableSet<String> CAR_CATEGORIES = ImmutableSet.of("REGULAR", "SUV");
  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private DriverTypeService driverTypeService;
  @Mock
  private Map<String, Object> context;
  @InjectMocks
  private DriverTypeEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance.setDriverTypeCache(driverTypeCache);
    testedInstance.setDriverTypeService(driverTypeService);

    when(context.get(eq(DriverEligibilityCheckContext.CAR_CATEGORIES))).thenReturn(CAR_CATEGORIES);
  }

  @Test
  public void testCheckReturnsNoErrorIfDriverTypesIsEmpty() throws Exception {
    when(context.get(eq(DriverEligibilityCheckContext.DRIVER_TYPES))).thenReturn(Collections.emptySet());
    ActiveDriver activeDriver = new ActiveDriver();

    assertSuccess(activeDriver);
  }

  @Test
  public void testCheckReturnsNoErrorIfDriverTypesMatchAndCarCategoriesAreSupported() {
    when(context.get(eq(DriverEligibilityCheckContext.DRIVER_TYPES))).thenReturn(WOMEN_ONLY);
    ActiveDriver activeDriver = new ActiveDriver();
    Driver driver = new Driver();
    driver.setGrantedDriverTypesBitmask(1);
    activeDriver.setDriver(driver);
    when(driverTypeCache.fromBitMask(anyInt())).thenReturn(WOMEN_ONLY);
    when(driverTypeService.checkIfDriverTypesSupportCarCategories(anySet(), anySet(), anyLong())).thenReturn(true);

    assertSuccess(activeDriver);
  }

  @Test
  public void testCheckReturnsErrorIfDriverTypesIsNotEmptyAndActiveDriverDoesntHaveGrantedTypes() {
    when(context.get(eq(DriverEligibilityCheckContext.DRIVER_TYPES))).thenReturn(WOMEN_ONLY);
    ActiveDriver activeDriver = new ActiveDriver();
    Driver driver = new Driver();
    activeDriver.setDriver(driver);

    assertError(activeDriver);
  }

  @Test
  public void testCheckReturnsErrorIfDriverTypesDoesntMatch() {
    when(context.get(eq(DriverEligibilityCheckContext.DRIVER_TYPES))).thenReturn(WOMEN_ONLY);
    ActiveDriver activeDriver = new ActiveDriver();
    Driver driver = new Driver();
    driver.setGrantedDriverTypesBitmask(1);
    activeDriver.setDriver(driver);
    when(driverTypeCache.fromBitMask(anyInt())).thenReturn(Collections.emptySet());

    assertError(activeDriver);
  }

  @Test
  public void testCheckReturnsErrorIfCarCategoriesAreNotSupported() {
    when(context.get(eq(DriverEligibilityCheckContext.DRIVER_TYPES))).thenReturn(WOMEN_ONLY);
    ActiveDriver activeDriver = new ActiveDriver();
    Driver driver = new Driver();
    driver.setGrantedDriverTypesBitmask(1);
    activeDriver.setDriver(driver);
    when(driverTypeCache.fromBitMask(anyInt())).thenReturn(WOMEN_ONLY);
    when(driverTypeService.checkIfDriverTypesSupportCarCategories(anySet(), anySet(), anyLong())).thenReturn(false);

    assertError(activeDriver);
  }

  private void assertError(ActiveDriver activeDriver) {
    Optional<EligibilityCheckError> result = testedInstance.check(activeDriver);

    assertTrue(result.isPresent());
    assertEquals(DriverTypeEligibilityCheck.MESSAGE+CAR_CATEGORIES+" "+WOMEN_ONLY, result.get().getMessage());
  }

  private void assertSuccess(ActiveDriver activeDriver) {
    Optional<EligibilityCheckError> result = testedInstance.check(activeDriver);
    assertFalse(result.isPresent());
  }

}