package com.rideaustin.report.entry;

import java.time.LocalDate;

import com.rideaustin.report.FormatAs;
import com.rideaustin.report.ReportField;
import com.rideaustin.report.TupleConsumer;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class AvatarTripCountReportEntry implements TupleConsumer {

  protected final Long avatarId;
  @ReportField(order = 0, format = FormatAs.DATE)
  private final LocalDate endOfWeek;
  @ReportField(order = 2)
  private final String firstName;
  @ReportField(order = 3)
  private final String lastName;
  @ReportField(order = 4)
  private final String email;
  @ReportField(order = 5)
  private final Long tripCountPerWeek;

}
