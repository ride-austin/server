package com.rideaustin.driverstatistic.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.driverstatistic.model.DriverId;
import com.rideaustin.driverstatistic.model.DriverStatistic;
import com.rideaustin.driverstatistic.model.DriverStatisticNotFoundException;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.DriverDslRepository;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
@RequestMapping(path = "/rest/drivers/{driverId}/stats", produces = MediaType.APPLICATION_JSON_VALUE)
public class DriverStatistics {

  private final DriverDslRepository driverDslRepository;
  private final DriverStatisticDtoAssembler assembler;

  @GetMapping
  @DriverEndpoint
  @ApiOperation("Get acceptance statistics for a driver")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Stats not found for a driver")
  })
  public List<DriverStatisticDto> get(@ApiParam(value = "Driver ID", example = "1") @PathVariable Long driverId) throws DriverStatisticNotFoundException {

    DriverId id = new DriverId(driverId);
    if (driverDslRepository.findById(driverId) == null) {
      throw new DriverStatisticNotFoundException(id);
    }

    DriverStatistic statistic = DriverStatistic.findOrCreate(id);

    return assembler.toDto(statistic);
  }

  @ResponseBody
  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handle(DriverStatisticNotFoundException e) {
    log.debug(e.getMessage(), e);
    return ResponseEntity.badRequest().body(e.getMessage());
  }

}
