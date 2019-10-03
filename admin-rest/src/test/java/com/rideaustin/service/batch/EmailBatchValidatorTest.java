package com.rideaustin.service.batch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.model.DriverBatchUpdateError;

public class EmailBatchValidatorTest {

  @Mock
  private DriverDslRepository driverDslRepository;

  private EmailBatchValidator testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new EmailBatchValidator(driverDslRepository);
  }

  @Test
  public void validateRecordReturnsNoErrorWhenNoDriverWithEmailExists() {
    final DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setEmail("abc@ed.ee");

    final Optional<DriverBatchUpdateError> result = testedInstance.validate(1, record);

    assertFalse(result.isPresent());
  }

  @Test
  public void validateRecordReturnsNoErrorWhenDriverWithEmailExistsAndIsTheSameAsRecord() {
    final long driverId = 1L;
    final String email = "abc@ed.ee";
    final DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setEmail(email);
    record.setId(driverId);
    final Driver driver = new Driver();
    driver.setId(driverId);
    when(driverDslRepository.findByEmail(record.getEmail())).thenReturn(driver);

    final Optional<DriverBatchUpdateError> result = testedInstance.validate(1, record);

    assertFalse(result.isPresent());
  }

  @Test
  public void validateRecordReturnsErrorWhenOtherDriverWithEmailExists() {
    final long driverId = 1L;
    final String email = "abc@ed.ee";
    final DriverBatchUpdateDto record = new DriverBatchUpdateDto();
    record.setEmail(email);
    record.setId(driverId);
    final Driver driver = new Driver();
    driver.setId(2L);
    when(driverDslRepository.findByEmail(record.getEmail())).thenReturn(driver);

    final Optional<DriverBatchUpdateError> result = testedInstance.validate(1, record);

    assertTrue(result.isPresent());
    assertEquals(2, result.get().getRowNumber());
    assertEquals("Email", result.get().getField());
    assertEquals(record.getEmail(), result.get().getValue());
    assertEquals("Provided email is already used by another driver", result.get().getMessage());
  }

  @Test
  public void validateFieldReturnsNoErrorWhenEmailIsValid() {
    final Optional<DriverBatchUpdateError> result = testedInstance.validate("abc@mail.com", null, 1);

    assertFalse(result.isPresent());
  }

  @Test
  public void validateFieldReturnsErrorWhenEmailIsInvalid() {
    final String email = "invalid";
    final Optional<DriverBatchUpdateError> result = testedInstance.validate(email, null, 1);

    assertTrue(result.isPresent());
    assertEquals(2, result.get().getRowNumber());
    assertEquals("Email", result.get().getField());
    assertEquals(email, result.get().getValue());
    assertEquals("Email address is incorrect", result.get().getMessage());
  }
}