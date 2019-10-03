package com.rideaustin.events.listeners;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.events.CityApprovalStatusUpdateEvent;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DocumentDslRepository;

public class CityApprovalStatusUpdateEventListenerTest {

  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private Driver driver;
  @Mock
  private Document card;

  private CityApprovalStatusUpdateEventListener testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new CityApprovalStatusUpdateEventListener(documentDslRepository);
  }

  @Test
  public void testHandleCityApprovalStatusUpdateResolvesNewDocumentStatus() throws Exception {
    Map<CityApprovalStatus, DocumentStatus> mapping = ImmutableMap.of(
      CityApprovalStatus.APPROVED, DocumentStatus.APPROVED,
      CityApprovalStatus.EXPIRED, DocumentStatus.EXPIRED,
      CityApprovalStatus.PENDING, DocumentStatus.PENDING,
      CityApprovalStatus.REJECTED_BY_CITY, DocumentStatus.REJECTED,
      CityApprovalStatus.REJECTED_PHOTO, DocumentStatus.REJECTED
    );
    when(documentDslRepository.findByAvatarAndType(eq(driver), eq(DocumentType.TNC_CARD))).thenReturn(card);

    for (Map.Entry<CityApprovalStatus, DocumentStatus> entry : mapping.entrySet()) {
      CityApprovalStatusUpdateEvent event = new CityApprovalStatusUpdateEvent(entry.getKey(), driver);
      testedInstance.handleCityApprovalStatusUpdate(event);
      verify(card, atLeastOnce()).setDocumentStatus(eq(entry.getValue()));
    }
  }

  @Test
  public void testHandleCityApprovalStatusUpdateSavesDocumentIfItsFound() {
    when(documentDslRepository.findByAvatarAndType(eq(driver), eq(DocumentType.TNC_CARD))).thenReturn(card);

    CityApprovalStatusUpdateEvent event = new CityApprovalStatusUpdateEvent(CityApprovalStatus.APPROVED, driver);
    testedInstance.handleCityApprovalStatusUpdate(event);

    verify(documentDslRepository).save(card);
  }

}