package com.rideaustin.service.batch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.model.DriverBatchUpdateError;

public class ActivationBatchValidatorTest {

  @Mock
  private DriverDslRepository driverDslRepository;

  private ActivationBatchValidator testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new ActivationBatchValidator(driverDslRepository);
  }

  @Test
  public void testValidateReturnsNoErrorIfDriverIsNotFound() {
    when(driverDslRepository.findById(anyLong())).thenReturn(null);

    Optional<DriverBatchUpdateError> result = testedInstance.validate(0, new DriverBatchUpdateDto());

    assertFalse(result.isPresent());
  }

  @Test
  public void testValidateReturnsNoErrorIfActiveAndActivationStatusNotUpdated() {
    when(driverDslRepository.findById(anyLong())).thenReturn(new Driver());

    Optional<DriverBatchUpdateError> result = testedInstance.validate(0, new DriverBatchUpdateDto());

    assertFalse(result.isPresent());
  }

  @Test
  public void testValidateReturnsErrorIfActiveIsUpdatedToTrueAndStatusNotUpdatedInconsistently() {
    DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setActive(true);

    for (DriverActivationStatus status : EnumSet.complementOf(EnumSet.of(DriverActivationStatus.ACTIVE))) {
      Driver driver = new Driver();
      driver.setActivationStatus(status);
      when(driverDslRepository.findById(anyLong())).thenReturn(driver);

      Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);
      assertTrue(status.toString(), result.isPresent());
    }
  }

  @Test
  public void testValidateReturnsErrorIfActiveIsUpdatedToFalseAndStatusNotUpdatedInconsistently() {
    DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setActive(false);
    Driver driver = new Driver();
    driver.setActivationStatus(DriverActivationStatus.ACTIVE);
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);

    Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);

    assertTrue(result.isPresent());
  }

  @Test
  public void testValidateReturnsErrorIfStatusUpdatedToActiveAndActiveIsFalseInconsistently() {
    DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setActivationStatus(DriverActivationStatus.ACTIVE);
    Driver driver = new Driver();
    driver.setActive(false);

    when(driverDslRepository.findById(anyLong())).thenReturn(driver);

    Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);

    assertTrue(result.isPresent());
  }

  @Test
  public void testValidateReturnsErrorIfStatusUpdatedToNotActiveAndActiveIsTrueInconsistently() {
    Driver driver = new Driver();
    driver.setActive(true);
    when(driverDslRepository.findById(anyLong())).thenReturn(driver);

    for (DriverActivationStatus status : EnumSet.complementOf(EnumSet.of(DriverActivationStatus.ACTIVE))) {
      DriverBatchUpdateDto record = new DriverBatchUpdateDto();
      record.setActivationStatus(status);
      Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);

      assertTrue(status.toString(), result.isPresent());
    }
  }

  @Test
  public void testValidateReturnsNoErrorIfActiveAndActivationStatusAreUpdatedConsistentlyAndSetToTrue() {
    DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setActive(true);
    record.setActivationStatus(DriverActivationStatus.ACTIVE);
    when(driverDslRepository.findById(anyLong())).thenReturn(new Driver());

    Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);

    assertFalse(result.isPresent());
  }

  @Test
  public void testValidateReturnsNoErrorIfActiveAndActivationStatusAreUpdatedConsistentlyAndSetToFalse() {
    when(driverDslRepository.findById(anyLong())).thenReturn(new Driver());
    DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setActive(false);

    for (DriverActivationStatus status : EnumSet.complementOf(EnumSet.of(DriverActivationStatus.ACTIVE))) {
      record.setActivationStatus(status);

      Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);

      assertFalse(status.toString(), result.isPresent());
    }
  }

  @Test
  public void testValidateReturnsErrorIfActiveAndActivationStatusAreUpdatedInconsistentlyAndSetToTrue() {
    when(driverDslRepository.findById(anyLong())).thenReturn(new Driver());
    DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setActive(true);

    for (DriverActivationStatus status : EnumSet.complementOf(EnumSet.of(DriverActivationStatus.ACTIVE))) {
      record.setActivationStatus(status);

      Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);

      assertTrue(status.toString(), result.isPresent());
    }
  }

  @Test
  public void testValidateReturnsErrorIfActiveAndActivationStatusAreUpdatedInconsistentlyAndSetToFalse() {
    when(driverDslRepository.findById(anyLong())).thenReturn(new Driver());
    DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setActive(false);
    record.setActivationStatus(DriverActivationStatus.ACTIVE);

    Optional<DriverBatchUpdateError> result = testedInstance.validate(0, record);

    assertTrue(result.isPresent());
  }

}