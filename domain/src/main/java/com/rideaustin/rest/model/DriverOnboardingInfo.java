package com.rideaustin.rest.model;

import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.PayoneerStatus;

public interface DriverOnboardingInfo {
  DriverActivationStatus getActivationStatus();

  CityApprovalStatus getCityApprovalStatus();

  PayoneerStatus getPayoneerStatus();
}
