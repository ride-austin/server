package com.rideaustin.model.reports;

import java.math.BigDecimal;
import java.time.Instant;

import com.querydsl.core.Tuple;
import com.rideaustin.report.TupleConsumer;
import com.rideaustin.utils.SafeZeroUtils;

import lombok.Getter;

@Getter
public class WeeklyStatisticsReportResultEntry implements TupleConsumer {

  private final Instant completedOn;
  private final String carCategory;
  private final BigDecimal surgeFare;
  private final BigDecimal surgeFactor;
  private final BigDecimal subTotal;
  private final BigDecimal bookingFee;
  private final BigDecimal cityFee;
  private final BigDecimal normalFare;
  private final BigDecimal driverPayment;
  private final BigDecimal raFixedFee;
  private final BigDecimal cancellationFee;
  private final BigDecimal tip;
  private final BigDecimal roundUp;
  private final BigDecimal distanceTravelled;

  public WeeklyStatisticsReportResultEntry(Tuple tuple) {
    int index = 0;
    completedOn = getInstantFromTimestamp(tuple, index++);
    carCategory = getString(tuple, index++);
    surgeFare = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    surgeFactor = getBigDecimal(tuple, index++);
    subTotal = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    bookingFee = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    cityFee = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    normalFare = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    driverPayment = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    raFixedFee = getBigDecimal(tuple, index++);
    cancellationFee = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    tip = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    roundUp = SafeZeroUtils.safeZeroAmount(getMoney(tuple, index++));
    distanceTravelled = getBigDecimal(tuple, index);
  }

}
