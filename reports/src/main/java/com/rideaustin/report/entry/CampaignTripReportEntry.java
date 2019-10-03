package com.rideaustin.report.entry;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

import org.joda.money.Money;

import com.querydsl.core.annotations.QueryProjection;
import com.rideaustin.Constants;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.report.ReportField;

import lombok.Getter;

@Getter
public class CampaignTripReportEntry {

  @ReportField(order = 1)
  private final LocalDateTime requestedAt;
  @ReportField(order = 2)
  private final String startAddress;
  @ReportField(order = 3)
  private final double startLocationLat;
  @ReportField(order = 4)
  private final double startLocationLng;
  @ReportField(order = 5)
  private final String endAddress;
  @ReportField(order = 6)
  private final double endLocationLat;
  @ReportField(order = 7)
  private final double endLocationLng;
  @ReportField(order = 8)
  private final double distance;
  @ReportField(order = 9, name = "Ride duration (seconds)")
  private final long duration;
  @ReportField(order = 10)
  private final Money fareTotal;
  @ReportField(order = 11)
  private final Money rideCost;
  @ReportField(order = 12)
  private final Money totalCharge;
  @ReportField(order = 13)
  private final String driverFirstName;
  @ReportField(order = 14)
  private final String driverLastName;
  @ReportField(order = 15)
  private final String driverEmail;
  @ReportField(order = 16)
  private final String riderFirstName;
  @ReportField(order = 17)
  private final String riderLastName;
  @ReportField(order = 18)
  private final String riderEmail;
  @ReportField(order = 19)
  private final double driverRating;
  @ReportField(order = 20)
  private final double riderRating;
  @ReportField(order = 21, name = "Pickup time (seconds)")
  private final long pickupTime;

  @QueryProjection
  public CampaignTripReportEntry(Date requestedAt, String startAddress, double startLocationLat, double startLocationLng,
    String endAddress, double endLocationLat, double endLocationLng, BigDecimal distance, Date acceptedOn, Date startedOn, Date completedOn,
    FareDetails fareDetails, String driverFirstName, String driverLastName, String driverEmail, String riderFirstName,
    String riderLastName, String riderEmail, double driverRating, double riderRating) {
    this.requestedAt = requestedAt.toInstant().atZone(Constants.CST_ZONE).toLocalDateTime();
    this.startAddress = startAddress;
    this.startLocationLat = startLocationLat;
    this.startLocationLng = startLocationLng;
    this.endAddress = endAddress;
    this.endLocationLat = endLocationLat;
    this.endLocationLng = endLocationLng;
    this.distance = Constants.MILES_PER_METER.multiply(safeZero(distance)).doubleValue();
    this.duration = (completedOn.getTime() - startedOn.getTime()) / 1000;
    this.fareTotal = fareDetails.getFareTotal();
    this.rideCost = fareDetails.getRideCost();
    this.totalCharge = fareDetails.getTotalCharge();
    this.driverFirstName = driverFirstName;
    this.driverLastName = driverLastName;
    this.driverEmail = driverEmail;
    this.riderFirstName = riderFirstName;
    this.riderLastName = riderLastName;
    this.riderEmail = riderEmail;
    this.driverRating = driverRating;
    this.riderRating = riderRating;
    this.pickupTime = (startedOn.getTime() - acceptedOn.getTime()) / 1000;
  }
}
