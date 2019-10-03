package com.rideaustin.service.eligibility.checks;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rideaustin.model.DocumentDto;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Driver.class)
public class ValidTNCCardEligibilityCheck extends BaseTNCCardEligilibiltyCheck {

  @Override
  protected Optional<EligibilityCheckError> raiseError() {
    return Optional.of(new EligibilityCheckError("TNC Card is not verified"));
  }

  @Override
  protected boolean doCheck(DocumentDto document) {
    return DocumentStatus.APPROVED.equals(document.getDocumentStatus());
  }
}
