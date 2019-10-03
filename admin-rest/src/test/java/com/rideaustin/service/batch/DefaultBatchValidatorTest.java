package com.rideaustin.service.batch;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.service.model.DriverBatchUpdateError;

public class DefaultBatchValidatorTest {

  private DefaultBatchValidator testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new DefaultBatchValidator();
  }

  @Test
  public void testValidateEnumReturnsNoErrorOnValidValue() throws NoSuchFieldException {
    Optional<DriverBatchUpdateError> result = testedInstance.validate("A", TestClass.class.getDeclaredField("enumField"), 0);

    assertFalse(result.isPresent());
  }

  @Test
  public void testValidateEnumReturnsErrorOnInvalidValue() throws NoSuchFieldException {
    Optional<DriverBatchUpdateError> result = testedInstance.validate("B", TestClass.class.getDeclaredField("enumField"), 0);

    assertTrue(result.isPresent());
  }

  @Test
  public void testValidateDateReturnsNoErrorOnValidValue() throws NoSuchFieldException {
    Optional<DriverBatchUpdateError> result = testedInstance.validate("25/12/1996", TestClass.class.getDeclaredField("dateField"), 0);

    assertFalse(result.isPresent());
  }

  @Test
  public void testValidateDateReturnsErrorOnInvalidValue() throws NoSuchFieldException {
    Optional<DriverBatchUpdateError> result = testedInstance.validate("1999/12/25", TestClass.class.getDeclaredField("enumField"), 0);

    assertTrue(result.isPresent());
  }

  private enum TestEnum {
    A
  }

  private static class TestClass {
    TestEnum enumField;
    Date dateField;
  }
}