package com.rideaustin.utils;

import java.util.Set;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;

public final class LicenseStateValidator implements ConstraintValidator<LicenseState, String> {

  @VisibleForTesting
  static final Set<String> STATES = Sets.newHashSet(
    "AL",
    "AK",
    "AZ",
    "AR",
    "CA",
    "CO",
    "CT",
    "DC",
    "DE",
    "FL",
    "GA",
    "HI",
    "ID",
    "IL",
    "IN",
    "IA",
    "KS",
    "KY",
    "LA",
    "ME",
    "MD",
    "MA",
    "MI",
    "MN",
    "MS",
    "MO",
    "MT",
    "NE",
    "NV",
    "NH",
    "NJ",
    "NM",
    "NY",
    "NC",
    "ND",
    "OH",
    "OK",
    "OR",
    "PA",
    "RI",
    "SC",
    "SD",
    "TN",
    "TX",
    "UT",
    "VT",
    "VA",
    "WA",
    "WV",
    "WI",
    "WY"
  );

  @Override
  public void initialize(LicenseState licenseState) {
    //do nothing
  }

  @Override
  public boolean isValid(String state, ConstraintValidatorContext constraintValidatorContext) {
    return STATES.contains(state);
  }
}