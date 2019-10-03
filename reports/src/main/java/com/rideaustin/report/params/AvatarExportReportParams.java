package com.rideaustin.report.params;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
abstract class AvatarExportReportParams {

  private String name;
  private String email;
  private Boolean active;
  private Boolean enabled;

}
