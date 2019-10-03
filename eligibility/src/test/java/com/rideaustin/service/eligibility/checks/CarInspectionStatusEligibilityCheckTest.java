package com.rideaustin.service.eligibility.checks;

import com.rideaustin.model.City;
import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.service.CityService;
import com.rideaustin.service.eligibility.EligibilityCheckError;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class CarInspectionStatusEligibilityCheckTest {

  private CarInspectionStatusEligibilityCheck testedInstance;
  @Mock
  private CityService cityService;

  private static final Object MESSAGE = "Your vehicle must pass inspection in order to activate it. Please send an email to documents@example.com";

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(cityService.getCityForCurrentClientAppVersionContext()).thenReturn(generateCity());
    testedInstance = new CarInspectionStatusEligibilityCheck(cityService);
  }

  @Test
  public void testCheckReturnsNoErrorIfCarIsApproved() {
    Car car = new Car();
    car.setInspectionStatus(CarInspectionStatus.APPROVED);

    Optional<EligibilityCheckError> result = testedInstance.check(car);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsErrorIfCarIsNull() {

    Optional<EligibilityCheckError> result = testedInstance.check(null);

    assertTrue(result.isPresent());
    assertEquals(MESSAGE, result.get().getMessage());
  }

  @Test
  public void testCheckReturnsErrorIfCarIsNotApproved() {
    EnumSet<CarInspectionStatus> statuses = EnumSet.complementOf(EnumSet.of(CarInspectionStatus.APPROVED));
    for (CarInspectionStatus status : statuses) {
      Car car = new Car();
      car.setInspectionStatus(status);

      Optional<EligibilityCheckError> result = testedInstance.check(car);

      assertTrue(result.isPresent());
      assertEquals(MESSAGE, result.get().getMessage());
    }
  }

  private City generateCity() {
    City city = new City();
    city.setDocumentsEmail("documents@example.com");
    return city;
  }

}