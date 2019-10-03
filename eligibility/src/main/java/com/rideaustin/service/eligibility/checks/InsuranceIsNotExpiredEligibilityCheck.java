package com.rideaustin.service.eligibility.checks;

import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.Document;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.repo.dsl.CarDocumentDslRepository;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Car.class)
public class InsuranceIsNotExpiredEligibilityCheck extends BaseValidityDateEligibilityCheck<Car> {

  static final String MESSAGE = "Car insurance expired! Please update insurance info";

  private final CarDocumentDslRepository carDocumentDslRepository;

  @Inject
  public InsuranceIsNotExpiredEligibilityCheck(CarDocumentDslRepository carDocumentDslRepository) {
    this.carDocumentDslRepository = carDocumentDslRepository;
  }

  protected Date resolveValidityDate(Car subject) {
    Date validityDate;
    Document insurance = carDocumentDslRepository.findByCarAndType(subject.getId(), DocumentType.INSURANCE);
    if (insurance != null) {
      validityDate = insurance.getValidityDate();
    } else {
      validityDate = subject.getDriver().getInsuranceExpiryDate();
    }
    return validityDate;
  }

  @Override
  protected Optional<EligibilityCheckError> raiseError() {
    return Optional.of(new EligibilityCheckError(MESSAGE));
  }
}
