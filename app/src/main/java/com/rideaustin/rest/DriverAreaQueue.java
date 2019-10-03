package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.model.AreaQueuePositions;
import com.rideaustin.service.areaqueue.AreaQueueService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverAreaQueue {

  private final AreaQueueService areaQueueService;

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Get lengths and driver positions for the queue-enabled areas")
  @GetMapping(path = "/rest/queues", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<AreaQueuePositions> getActiveAreaDetails(@ApiParam(value = "City ID", example = "1", required = true) @RequestParam(defaultValue = "1") Long cityId) {
    return areaQueueService.getActiveAreaDetails(cityId);
  }
  
}
