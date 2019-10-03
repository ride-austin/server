package com.rideaustin.report.params;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromocodeUsageReportParams {
  private String codeLiteral;
  private boolean completedOnly;

}
