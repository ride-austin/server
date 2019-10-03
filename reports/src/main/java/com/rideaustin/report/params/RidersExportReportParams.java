package com.rideaustin.report.params;

import com.rideaustin.rest.model.ListRidersParams;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RidersExportReportParams extends AvatarExportReportParams {
  private Long riderId;

  public RidersExportReportParams(ListRidersParams listRidersParams) {
    super(listRidersParams.getName(), listRidersParams.getEmail(), listRidersParams.getActive(), listRidersParams.getEnabled());
    this.riderId = listRidersParams.getRiderId();
  }

  public ListRidersParams asListRidersParams() {
    ListRidersParams listRidersParams = new ListRidersParams();
    listRidersParams.setName(getName());
    listRidersParams.setEmail(getEmail());
    listRidersParams.setActive(getActive());
    listRidersParams.setEnabled(getEnabled());
    listRidersParams.setRiderId(riderId);
    return listRidersParams;
  }

}
