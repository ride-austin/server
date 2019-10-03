package com.rideaustin.clients.configuration;

import java.util.Map;

import com.rideaustin.filter.ClientType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.Location;

public interface ConfigurationElement {
  Map getConfiguration(ClientType clientType, Location location, Long cityId) throws RideAustinException;

  Map getDefaultConfiguration(ClientType clientType, Location location, Long cityId) throws RideAustinException;
}
