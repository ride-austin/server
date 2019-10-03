package com.rideaustin.service.eligibility.checks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.DocumentDto;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.eligibility.EligibilityCheckError;

public class ValidTNCCardEligibilityCheckTest {

  private ValidTNCCardEligibilityCheck testedInstance;
  @Mock
  protected DocumentService documentService;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new ValidTNCCardEligibilityCheck();
    testedInstance.setDocumentService(documentService);
  }

  @Test
  public void testCheckReturnsNoErrorIfDriverIsNull() throws Exception {
    Optional<EligibilityCheckError> result = testedInstance.check(null);

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsNoErrorIfTNCCardIsVerified() {
    DocumentDto document = new DocumentDto(1L, null, DocumentStatus.APPROVED, null, null,
      null, 1L, new Date());
    when(documentService.listAvatarDocuments(any())).thenReturn(Collections.singletonList(document));

    Optional<EligibilityCheckError> result = testedInstance.check(new Driver());

    assertFalse(result.isPresent());
  }

  @Test
  public void testCheckReturnsErrorIfTNCCardIsExpired() {
    DocumentDto document = new DocumentDto(1L, null, DocumentStatus.EXPIRED, null, null,
      null, 1L, new Date());
    when(documentService.listAvatarDocuments(any())).thenReturn(Collections.singletonList(document));

    Optional<EligibilityCheckError> result = testedInstance.check(new Driver());

    assertTrue(result.isPresent());
    assertEquals(testedInstance.raiseError().get().getMessage(), result.get().getMessage());
  }

  @Test
  public void testCheckReturnsErrorIfCardIsNotProvided() {
    when(documentService.listAvatarDocuments(any())).thenReturn(Collections.emptyList());

    Optional<EligibilityCheckError> result = testedInstance.check(new Driver());

    assertTrue(result.isPresent());
    assertEquals("TNC card is not provided", result.get().getMessage());
  }

}