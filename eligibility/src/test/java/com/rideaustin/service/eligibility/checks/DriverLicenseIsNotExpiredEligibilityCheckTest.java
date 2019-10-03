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
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.service.eligibility.EligibilityCheckError;

public class DriverLicenseIsNotExpiredEligibilityCheckTest {

  @Mock
  private DocumentDslRepository documentDslRepository;

  private DriverLicenseIsNotExpiredEligibilityCheck testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DriverLicenseIsNotExpiredEligibilityCheck(documentDslRepository);
  }

  @Test
  public void checkRaisesErrorWhenLicenseIsNull() {
    final Driver subject = new Driver();
    when(documentDslRepository.findByAvatarAndType(eq(subject), eq(DocumentType.LICENSE))).thenReturn(null);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(DriverLicenseIsNotExpiredEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void checkRaisesErrorWhenValidityDateIsNull() {
    final Driver subject = new Driver();
    when(documentDslRepository.findByAvatarAndType(eq(subject), eq(DocumentType.LICENSE))).thenReturn(new Document());

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(DriverLicenseIsNotExpiredEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void checkRaisesErrorWhenValidityDateBeforeNow() {
    final Driver subject = new Driver();
    final Document license = new Document();
    license.setValidityDate(Date.from(Instant.now().minus(10, ChronoUnit.DAYS)));
    when(documentDslRepository.findByAvatarAndType(eq(subject), eq(DocumentType.LICENSE))).thenReturn(license);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertTrue(result.isPresent());
    assertEquals(DriverLicenseIsNotExpiredEligibilityCheck.MESSAGE, result.get().getMessage());
  }

  @Test
  public void checkRaisesNoErrorWhenValidityDateAfterNow() {
    final Driver subject = new Driver();
    final Document license = new Document();
    license.setValidityDate(Date.from(Instant.now().plus(10, ChronoUnit.DAYS)));
    when(documentDslRepository.findByAvatarAndType(eq(subject), eq(DocumentType.LICENSE))).thenReturn(license);

    final Optional<EligibilityCheckError> result = testedInstance.check(subject);

    assertFalse(result.isPresent());
  }
}