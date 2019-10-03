package com.rideaustin.service.onboarding;

import java.util.EnumSet;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.rideaustin.model.enums.DocumentStatus;

@Component
public class PhotoOnboardingStatusCheck extends DocumentOnboardingStatusCheck {
  @Override
  protected Set<DocumentStatus> pendingValues() {
    return EnumSet.of(DocumentStatus.PENDING, DocumentStatus.REJECTED);
  }
}
