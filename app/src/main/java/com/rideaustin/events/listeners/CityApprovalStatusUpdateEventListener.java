package com.rideaustin.events.listeners;

import javax.inject.Inject;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rideaustin.events.CityApprovalStatusUpdateEvent;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.repo.dsl.DocumentDslRepository;

@Component
public class CityApprovalStatusUpdateEventListener {

  private final DocumentDslRepository documentDslRepository;

  @Inject
  public CityApprovalStatusUpdateEventListener(DocumentDslRepository documentDslRepository) {
    this.documentDslRepository = documentDslRepository;
  }

  @TransactionalEventListener
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void handleCityApprovalStatusUpdate(CityApprovalStatusUpdateEvent event) {
    DocumentStatus newDocumentStatus = resolveNewDocumentStatus(event);
    Document tncCard = documentDslRepository.findByAvatarAndType(event.getDriver(), DocumentType.TNC_CARD);
    if (tncCard != null) {
      tncCard.setDocumentStatus(newDocumentStatus);
      documentDslRepository.save(tncCard);
    }
  }

  private DocumentStatus resolveNewDocumentStatus(CityApprovalStatusUpdateEvent event) {
    DocumentStatus newDocumentStatus = null;
    switch (event.getStatus()) {
      case APPROVED:
        newDocumentStatus = DocumentStatus.APPROVED;
        break;
      case EXPIRED:
        newDocumentStatus = DocumentStatus.EXPIRED;
        break;
      case PENDING:
      case NOT_PROVIDED:
        newDocumentStatus = DocumentStatus.PENDING;
        break;
      case REJECTED_BY_CITY:
      case REJECTED_PHOTO:
        newDocumentStatus = DocumentStatus.REJECTED;
    }
    return newDocumentStatus;
  }
}
