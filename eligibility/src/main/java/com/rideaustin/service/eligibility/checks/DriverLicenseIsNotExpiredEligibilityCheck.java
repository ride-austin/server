package com.rideaustin.service.eligibility.checks;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Driver.class)
public class DriverLicenseIsNotExpiredEligibilityCheck extends BaseValidityDateEligibilityCheck<Driver> {

  static final String MESSAGE = "Driver license expired! Please update license info";
  private final DocumentDslRepository documentDslRepository;

  @Inject
  public DriverLicenseIsNotExpiredEligibilityCheck(DocumentDslRepository documentDslRepository) {
    this.documentDslRepository = documentDslRepository;
  }

  protected Date resolveValidityDate(Driver subject) {
    Date validityDate = null;
    Document license = documentDslRepository.findByAvatarAndType(subject, DocumentType.LICENSE);
    if (license != null && license.getValidityDate() != null) {
      validityDate = license.getValidityDate();
    }
    return validityDate;
  }

  @Override
  protected Optional<EligibilityCheckError> raiseError() {
    return Optional.of(new EligibilityCheckError(MESSAGE));
  }

}
