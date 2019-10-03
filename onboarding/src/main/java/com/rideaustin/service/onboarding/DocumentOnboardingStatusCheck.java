package com.rideaustin.service.onboarding;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentStatus;

@Component
public class DocumentOnboardingStatusCheck extends BaseOnboardingStatusCheck<DocumentStatus, Document, OnboardingStatusCheck.Context> {

  @Override
  protected Set<DocumentStatus> pendingValues() {
    return EnumSet.of(DocumentStatus.PENDING, DocumentStatus.EXPIRED, DocumentStatus.REJECTED);
  }

  @Override
  protected Set<DocumentStatus> finalReviewValues() {
    return EnumSet.of(DocumentStatus.APPROVED);
  }

  @Override
  protected Set<DocumentStatus> terminalValues() {
    return EnumSet.of(DocumentStatus.APPROVED);
  }

  @Override
  protected Supplier<DocumentStatus> value(Document document) {
    return document::getDocumentStatus;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return Document.class.isAssignableFrom(clazz);
  }
}
