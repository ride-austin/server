package com.rideaustin.test.fixtures.check;

import java.util.Optional;

import com.rideaustin.model.TermsAcceptance;
import com.rideaustin.repo.dsl.TermsDslRepository;

public class TermAcceptanceChecker implements RecordChecker<TermsAcceptance> {
  private final TermsDslRepository termsDslRepository;

  public TermAcceptanceChecker(TermsDslRepository termsDslRepository) {
    this.termsDslRepository = termsDslRepository;
  }

  @Override
  public Optional<TermsAcceptance> getIfExists(TermsAcceptance termsAcceptance) {
    return Optional.ofNullable(termsDslRepository.getTermsAcceptance(termsAcceptance.getDriver().getId(),
      termsAcceptance.getTerms().getId()));
  }
}
