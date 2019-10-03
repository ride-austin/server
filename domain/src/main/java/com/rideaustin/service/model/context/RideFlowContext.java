package com.rideaustin.service.model.context;

import java.util.Date;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class RideFlowContext {
  private long driver;
  private Long driverSession;
  private Date acceptedOn;
  private Date reachedOn;
  private Date startedOn;
  private boolean stacked;
  private int destinationUpdates = 0;

  public void increaseDestinationUpdatesCount() {
    this.destinationUpdates++;
  }

}
