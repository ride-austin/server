package com.rideaustin.report.params;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.report.CityToReport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class FingerprintStatusReportParams {

  private CityApprovalStatus fingerPrintCleared;
  private CityToReport city;


}
