package com.rideaustin.assemblers;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.rest.model.DirectConnectHistoryDto;
import com.rideaustin.service.DocumentService;

public class DirectConnectHistoryDtoEnricherTest {

  @Mock
  private DocumentService documentService;

  private DirectConnectHistoryDtoEnricher testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new DirectConnectHistoryDtoEnricher(documentService);
  }

  @Test
  public void enrichSkipsNull() {
    final DirectConnectHistoryDto result = testedInstance.enrich(null);

    assertNull(result);
  }

  @Test
  public void enrichSetsPhotoUrl() {
    final long driverId = 1L;
    DirectConnectHistoryDto source = new DirectConnectHistoryDto(driverId, "000", "A", "B", new Date());

    final Document document = new Document();
    document.setDocumentUrl("url");
    when(documentService.findAvatarDocument(eq(driverId), eq(DocumentType.DRIVER_PHOTO))).thenReturn(document);

    final DirectConnectHistoryDto result = testedInstance.enrich(source);

    assertEquals(document.getDocumentUrl(), result.getPhotoURL());
  }
}