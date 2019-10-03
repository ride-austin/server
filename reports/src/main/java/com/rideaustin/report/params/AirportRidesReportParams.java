package com.rideaustin.report.params;

import com.rideaustin.report.enums.AirportToReport;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AirportRidesReportParams extends BaseStartEndDateParams {

  private Double airportLatitudeFrom;
  private Double airportLongitudeFrom;
  private Double airportLatitudeTo;
  private Double airportLongitudeTo;
  private AirportToReport airport;

}
