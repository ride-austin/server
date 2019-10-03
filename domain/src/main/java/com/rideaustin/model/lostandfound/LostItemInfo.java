package com.rideaustin.model.lostandfound;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LostItemInfo {
  private Long rideId;
  private Long riderUserId;
  private Long riderId;
  private String riderEmail;
  private String riderFirstName;
  private String riderLastName;
  private String riderPhone;
  private String driverEmail;
  private String driverPhone;
  private String driverFirstName;
  private String driverLastName;
  private Long cityId;

  public String getRiderName() {
    return String.format("%s %s", riderFirstName, riderLastName);
  }

  public String getDriverName() {
    return String.format("%s %s", driverFirstName, driverLastName);
  }
}
