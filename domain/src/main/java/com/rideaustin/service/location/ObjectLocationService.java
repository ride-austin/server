package com.rideaustin.service.location;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.rideaustin.model.Area;
import com.rideaustin.model.LocationAware;
import com.rideaustin.model.enums.ActiveDriverStatus;
import com.rideaustin.service.location.enums.LocationType;

public interface ObjectLocationService<T extends LocationAware> {

  List<T> searchActiveInsideArea(Area area, Set<ActiveDriverStatus> statuses);

  T saveOrUpdateLocationObject(T object);

  void removeLocationObject(Long ownerId, LocationType type);

  List<T> getAll();

  T getById(Long ownerId, LocationType type);

  List<T> getByIds(Collection<Long> ownerIds, LocationType type);
}

