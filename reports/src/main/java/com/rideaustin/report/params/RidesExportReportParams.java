package com.rideaustin.report.params;

import java.time.Instant;
import java.util.List;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.rest.model.ListRidesParams;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RidesExportReportParams extends CompletedOnBeforeReportParams {

  private List<RideStatus> status;

  private String riderEmail;

  private String driverEmail;

  private Long riderId;

  private Long driverId;

  private Boolean charged = false;

  private Instant createdOnAfter;

  private Instant createdOnBefore;

  private Instant completedOnAfter;

  private Instant completedOnBefore;

  private Instant cancelledOnAfter;

  private Instant cancelledOnBefore;

  private String phoneNumber;

  private Long cityId;

  public RidesExportReportParams(ListRidesParams listRidesParams) {
    this(listRidesParams.getStatus(), listRidesParams.getRiderEmail(), listRidesParams.getDriverEmail(),
      listRidesParams.getRiderId(), listRidesParams.getDriverId(), listRidesParams.isCharged(),
      listRidesParams.getCreatedOnAfter(), listRidesParams.getCreatedOnBefore(), listRidesParams.getCompletedOnAfter(),
      listRidesParams.getCompletedOnBefore(), listRidesParams.getCancelledOnAfter(), listRidesParams.getCancelledOnBefore(),
      listRidesParams.getPhoneNumber(), listRidesParams.getCityId());
  }

  public ListRidesParams asListRidesParams() {
    ListRidesParams listRidesParams = new ListRidesParams();
    listRidesParams.setCancelledOnAfter(cancelledOnAfter);
    listRidesParams.setCancelledOnBefore(cancelledOnBefore);
    listRidesParams.setCharged(charged);
    listRidesParams.setCompletedOnAfter(completedOnAfter);
    listRidesParams.setCompletedOnBefore(getCompletedOnBefore());
    listRidesParams.setCreatedOnAfter(createdOnAfter);
    listRidesParams.setCreatedOnBefore(createdOnBefore);
    listRidesParams.setDriverEmail(driverEmail);
    listRidesParams.setDriverId(driverId);
    listRidesParams.setPhoneNumber(phoneNumber);
    listRidesParams.setStatus(status);
    listRidesParams.setRiderEmail(riderEmail);
    listRidesParams.setRiderId(riderId);
    listRidesParams.setCityId(cityId);
    return listRidesParams;
  }
}
