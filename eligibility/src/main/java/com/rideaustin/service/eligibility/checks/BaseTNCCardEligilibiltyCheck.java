package com.rideaustin.service.eligibility.checks;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.rideaustin.model.DocumentDto;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.rest.model.ListAvatarDocumentsParams;
import com.rideaustin.service.DocumentService;
import com.rideaustin.service.eligibility.EligibilityCheckError;

public abstract class BaseTNCCardEligilibiltyCheck extends BaseEligibilityCheckItem<Driver> {

  @Inject
  protected DocumentService documentService;

  public BaseTNCCardEligilibiltyCheck() {
    super(Collections.emptyMap());
  }

  @Override
  public Optional<EligibilityCheckError> check(Driver subject) {
    if (subject != null) {
      ListAvatarDocumentsParams params = new ListAvatarDocumentsParams();
      params.setAvatarId(subject.getId());
      params.setDocumentType(DocumentType.TNC_CARD);
      List<DocumentDto> documents = documentService.listAvatarDocuments(params);
      if (documents.isEmpty()) {
        return Optional.of(new EligibilityCheckError("TNC card is not provided"));
      }
      for (DocumentDto document : documents) {
        if (!doCheck(document)) {
          return raiseError();
        }
      }
    }
    return Optional.empty();
  }

  protected abstract Optional<EligibilityCheckError> raiseError();
  protected abstract boolean doCheck(DocumentDto document);

  public void setDocumentService(DocumentService documentService) {
    this.documentService = documentService;
  }
}
