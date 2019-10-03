package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.model.AreaQueueInfo;
import com.rideaustin.service.areaqueue.AreaQueueService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverAreaQueueAdministration {

  private final AreaQueueService areaQueueService;

  @GetMapping("/rest/queue/{areaId}/info")
  @ApiOperation("Get information on queue-enabled area as an admin")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Queue-enabled area is not found")
  })
  public AreaQueueInfo getAreaQueueInfo(@PathVariable long areaId) throws NotFoundException {
    return areaQueueService.getPositions(areaId);
  }
}
