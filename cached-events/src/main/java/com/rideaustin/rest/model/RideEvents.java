package com.rideaustin.rest.model;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RideEvents {

  private List<Map<String, String>> events;

}
