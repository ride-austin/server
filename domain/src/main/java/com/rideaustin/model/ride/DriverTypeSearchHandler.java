package com.rideaustin.model.ride;

import java.util.List;

import com.rideaustin.service.ActiveDriverSearchCriteria;
import com.rideaustin.service.QueuedActiveDriverSearchCriteria;
import com.rideaustin.service.model.OnlineDriverDto;

public interface DriverTypeSearchHandler {

  List<OnlineDriverDto> searchDrivers(ActiveDriverSearchCriteria searchCriteria);
  List<OnlineDriverDto> searchDrivers(QueuedActiveDriverSearchCriteria searchCriteria);

}
