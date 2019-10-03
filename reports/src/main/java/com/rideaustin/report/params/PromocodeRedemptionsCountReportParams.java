package com.rideaustin.report.params;

import com.rideaustin.report.enums.GroupByTimePeriod;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromocodeRedemptionsCountReportParams extends BaseStartEndDateParams {

  private GroupByTimePeriod groupByTimePeriod;
  private String codeLiteral;

}
