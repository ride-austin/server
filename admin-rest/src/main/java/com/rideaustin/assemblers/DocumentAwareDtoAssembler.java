package com.rideaustin.assemblers;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.service.DocumentService;

public abstract class DocumentAwareDtoAssembler<I extends BaseEntity, O> implements SingleSideAssembler<I, O> {

  protected final DocumentService documentService;

  protected DocumentAwareDtoAssembler(DocumentService documentService) {
    this.documentService = documentService;
  }

  protected DocumentStatus getDocumentStatus(I ds, Map<DocumentType, Map<Long, Document>> documents, DocumentType type) {
    Map<Long, Document> documentsToDrivers = Optional.ofNullable(documents.get(type))
      .orElse(Collections.emptyMap());
    return Optional.ofNullable(documentsToDrivers.get(ds.getId()))
      .map(Document::getDocumentStatus)
      .orElse(DocumentStatus.PENDING);
  }

  protected String getDocumentUrl(I ds, Map<DocumentType, Map<Long, Document>> documents, DocumentType type) {
    Map<Long, Document> documentsToDrivers = Optional.ofNullable(documents.get(type))
      .orElse(Collections.emptyMap());
    return Optional.ofNullable(documentsToDrivers.get(ds.getId()))
      .map(Document::getDocumentUrl)
      .orElse(null);
  }

  protected abstract O toDto(I ds, Map<DocumentType, Map<Long, Document>> documents);

  @Override
  public O toDto(I ds) {
    throw new UnsupportedOperationException();
  }
}
