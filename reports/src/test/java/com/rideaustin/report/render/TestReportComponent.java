package com.rideaustin.report.render;

import com.rideaustin.report.model.ReportComponent;

@ReportComponent(
  id = 1000,
  name = "Test report",
  header = "test",
  parameters = {
    @ReportComponent.Param(
      label = "Param 1",
      name = "Name 1",
      order = 1
    ),
    @ReportComponent.Param(
      label = "Param 2",
      name = "Name 2",
      order = 2,
      internal = true
    ),
    @ReportComponent.Param(
      label = "Param 3",
      name = "Name 3",
      order = 3
    ),
  }
)
public class TestReportComponent {
}
