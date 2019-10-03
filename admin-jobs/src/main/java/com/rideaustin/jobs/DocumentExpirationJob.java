package com.rideaustin.jobs;

import java.util.List;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.service.DocumentService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DocumentExpirationJob extends BaseJob {

  @Inject
  private DocumentDslRepository documentDslRepository;

  @Inject
  private DocumentService documentService;

  @Override
  protected void executeInternal() {
    List<Long> documentsAboutToExpire = documentDslRepository.findDocumentsIdsToExpireToday();
    documentsAboutToExpire.forEach(id -> {
      log.info("Updating document {} status to EXPIRED", id);
      try {
        documentService.updatedExpiredDocument(id);
      } catch (Exception ex) {
        log.error("Unable to update the status of expired document", ex);
      }
    });
  }

  @Override
  protected String getDescription() {
    return "Set document status to EXPIRED if validity date is in the past";
  }


  public void setDocumentDslRepository(DocumentDslRepository documentDslRepository) {
    this.documentDslRepository = documentDslRepository;
  }

  public void setDocumentService(DocumentService documentService) {
    this.documentService = documentService;
  }
}
