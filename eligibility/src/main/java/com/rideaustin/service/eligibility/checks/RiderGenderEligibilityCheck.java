package com.rideaustin.service.eligibility.checks;

import java.util.Collections;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.rideaustin.model.user.Gender;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.eligibility.EligibilityCheck;
import com.rideaustin.service.eligibility.EligibilityCheckError;

@Component
@EligibilityCheck(targetClass = Rider.class)
public class RiderGenderEligibilityCheck extends BaseEligibilityCheckItem<Rider> {

  public RiderGenderEligibilityCheck() {
    super(Collections.emptyMap());
  }

  @Override
  public Optional<EligibilityCheckError> check(Rider subject) {
    if (!Gender.FEMALE.equals(subject.getGender())) {
      return Optional.of(new EligibilityCheckError("Rider is not eligible to request Women Only ride"));
    }
    return Optional.empty();
  }

}
