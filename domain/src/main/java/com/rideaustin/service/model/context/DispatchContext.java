package com.rideaustin.service.model.context;

import com.rideaustin.service.model.DispatchCandidate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class DispatchContext {

  private long id;
  private long cityId;
  private double startLocationLat;
  private double startLocationLng;
  private DispatchCandidate candidate;
  private boolean accepted = false;
  private DispatchType dispatchType;

  public DispatchContext(long id, long cityId, double startLocationLat, double startLocationLng, DispatchType type) {
    this.id = id;
    this.cityId = cityId;
    this.startLocationLat = startLocationLat;
    this.startLocationLng = startLocationLng;
    this.dispatchType = type;
  }

}
