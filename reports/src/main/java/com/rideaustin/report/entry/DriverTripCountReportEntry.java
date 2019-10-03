package com.rideaustin.report.entry;

import java.time.LocalDate;

import com.rideaustin.report.ReportField;

public class DriverTripCountReportEntry extends AvatarTripCountReportEntry {

  public DriverTripCountReportEntry(Long avatarId, LocalDate endOfWeek, String firstName, String lastName, String email,
    Long tripCountPerWeek) {
    super(avatarId, endOfWeek, firstName, lastName, email, tripCountPerWeek);
  }

  @ReportField(order = 1, name = "Driver ID")
  public Long getDriverId() {
    return avatarId;
  }

}
