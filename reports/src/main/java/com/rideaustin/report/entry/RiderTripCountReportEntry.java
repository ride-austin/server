package com.rideaustin.report.entry;

import java.time.LocalDate;

import com.rideaustin.report.ReportField;

public class RiderTripCountReportEntry extends AvatarTripCountReportEntry {

  public RiderTripCountReportEntry(Long avatarId, LocalDate endOfWeek, String firstName, String lastName, String email,
    Long tripCountPerWeek) {
    super(avatarId, endOfWeek, firstName, lastName, email, tripCountPerWeek);
  }

  @ReportField(order = 1, name = "Rider ID")
  public Long getRiderId() {
    return avatarId;
  }

}
