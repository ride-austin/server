package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.service.eligibility.EligibilityCheckError;

public class InsuranceIsNotExpiredEligibilityCheckTest {

  @Mock
  private CarDocumentDslRepository carDocumentDslRepository;

  private InsuranceIsNotExpiredEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new InsuranceIsNotExpiredEligibilityCheck(carDocumentDslRepository);
  }

  @Test
  public void checkRaisesErrorWhenInsuranceIsNullAndDriverFieldIsNull() {
    final Car subject = new Car();
    subject.setId(1L);
    subject.setDriver(new Driver());
    when(carDocumentDslRepository.findByCarAndType(eq(subject.getId()), eq(DocumentType.INSURANCE))).thenReturn(null);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(InsuranceIsNotExpiredEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void checkRaisesErrorWhenInsuranceIsNullAndDriverFieldIsInThePast() {
    final Car subject = new Car();
    subject.setId(1L);
    final Driver driver = new Driver();
    driver.setInsuranceExpiryDate(Date.from(Instant.now().minus(10, ChronoUnit.DAYS)));
    subject.setDriver(driver);
    when(carDocumentDslRepository.findByCarAndType(eq(subject.getId()), eq(DocumentType.INSURANCE))).thenReturn(null);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(InsuranceIsNotExpiredEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void checkRaisesErrorWhenValidityDateIsNull() {
    final Car subject = new Car();
    subject.setId(1L);
    when(carDocumentDslRepository.findByCarAndType(eq(subject.getId()), eq(DocumentType.INSURANCE))).thenReturn(new Document());

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(InsuranceIsNotExpiredEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void checkRaisesErrorWhenValidityDateBeforeNow() {
    Document insurance = new Document();
    insurance.setValidityDate(Date.from(Instant.now().minus(10, ChronoUnit.DAYS)));
    final Car subject = new Car();
    subject.setId(1L);
    when(carDocumentDslRepository.findByCarAndType(eq(subject.getId()), eq(DocumentType.INSURANCE))).thenReturn(insurance);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(InsuranceIsNotExpiredEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void checkRaisesNoErrorWhenValidityDateAfterNow() {
    Document insurance = new Document();
    insurance.setValidityDate(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)));
    final Car subject = new Car();
    subject.setId(1L);
    when(carDocumentDslRepository.findByCarAndType(eq(subject.getId()), eq(DocumentType.INSURANCE))).thenReturn(insurance);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertFalse(result.isPresent());
  }

  @Test
  public void checkRaisesNoErrorWhenInsuranceIsNullAndDriverFieldIsInFuture() {
    final Car subject = new Car();
    subject.setId(1L);
    final Driver driver = new Driver();
    driver.setInsuranceExpiryDate(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)));
    subject.setDriver(driver);
    when(carDocumentDslRepository.findByCarAndType(eq(subject.getId()), eq(DocumentType.INSURANCE))).thenReturn(null);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertFalse(result.isPresent());
  }
}