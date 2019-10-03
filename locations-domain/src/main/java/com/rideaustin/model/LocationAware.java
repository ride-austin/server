package com.rideaustin.model;

import com.rideaustin.service.location.model.LocationObject;

public interface LocationAware {

  long getId();

  double getLatitude();

  double getLongitude();

  LocationObject getLocationObject();

  void setLocationObject(LocationObject locationObject);

}
