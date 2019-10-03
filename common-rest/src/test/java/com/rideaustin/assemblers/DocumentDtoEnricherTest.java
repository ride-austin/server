package com.rideaustin.assemblers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.DocumentDto;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.service.thirdparty.S3StorageService;

public class DocumentDtoEnricherTest {

  @Mock
  private S3StorageService s3StorageService;

  private DocumentDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DocumentDtoEnricher(s3StorageService);
  }

  @Test
  public void enrichSkipsNull() {
    final DocumentDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsSignedUrlForPrivateDocs() {
    DocumentDto source = new DocumentDto(1L, DocumentType.INSURANCE, DocumentStatus.APPROVED, "url",
      "name", "notes", 1L, new Date());

    final String signedUrl = "signed";
    when(s3StorageService.getSignedURL(anyString())).thenReturn(signedUrl);

    final DocumentDto result = testedInstance.enrich(source);

    verify(s3StorageService, only()).getSignedURL(anyString());
    assertEquals(signedUrl, result.getDocumentUrl());
  }

  @Test
  public void enrichSetsPlainUrlForPublicDocs() {
    final String url = "url";
    DocumentDto source = new DocumentDto(1L, DocumentType.DRIVER_PHOTO, DocumentStatus.APPROVED, url,
      "name", "notes", 1L, new Date());

    final DocumentDto result = testedInstance.enrich(source);

    verify(s3StorageService, never()).getSignedURL(anyString());
    assertEquals(url, result.getDocumentUrl());
  }
}