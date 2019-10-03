package com.rideaustin.rest;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;

import org.apache.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.CarDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.Car;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.CarDto;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DriverCarsService;
import com.rideaustin.service.DriverService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RequestMapping("/rest/drivers")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverCars {

  private final CurrentUserService cuSvc;
  private final DriverService driverService;
  private final DriverCarsService driverCarsService;

  private final CarDtoAssembler carDtoAssembler;

  @DriverEndpoint
  @RolesAllowed({AvatarType.ROLE_DRIVER, AvatarType.ROLE_ADMIN})
  @ApiOperation("Get all present driver's cars")
  @GetMapping(path = "/{driverId}/allCars", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public List<CarDto> getCars(@ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId) throws RideAustinException {
    Set<Car> cars = driverService.findDriver(driverId, cuSvc.getUser())
      .getCars()
      .stream()
      .filter(c -> !c.isRemoved())
      .collect(Collectors.toSet());
    return carDtoAssembler.toDto(cars);
  }

  @DriverEndpoint
  @ApiOperation("Set a car as the default for a driver")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @PutMapping(path = "/selected", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver or admin"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver or car is not found"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Trying to set unapproved or removed car as the default")
  })
  public CarDto selectCar(
    @ApiParam(value = "Driver ID", example = "1") @RequestParam Long driverId,
    @ApiParam(value = "Car ID", example = "1") @RequestParam Long carId
  ) throws RideAustinException {
    return carDtoAssembler.toDto(driverCarsService.selectDriverCar(carId, driverId));
  }

  @DriverEndpoint
  @WebClientEndpoint
  @ApiOperation("Add a new car to driver's profile")
  @RolesAllowed({AvatarType.ROLE_DRIVER, AvatarType.ROLE_ADMIN})
  @PostMapping(path = "/{driverId}/cars", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver or admin"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver is not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to upload car photo")
  })
  public CarDto addCar(@ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId,
    @ApiParam(required = true, name = "car") @RequestPart(name = "car") @Valid CarDto dto,
    @Deprecated @ApiParam @RequestPart(required = false) MultipartFile photo,
    @ApiParam @RequestPart(required = false) MultipartFile insurancePhoto)
    throws RideAustinException {
    return carDtoAssembler.toDto(driverCarsService.addCar(driverId, dto, photo, insurancePhoto));
  }

  @DriverEndpoint
  @WebClientEndpoint
  @ApiOperation("Update car information")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @PutMapping(path = "/{driverId}/cars/{carId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver or admin"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver, car or insurance is not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send a push notification to driver when car category is changed")
  })
  public CarDto editCar(@ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId,
    @ApiParam(value = "Car ID", example = "1") @PathVariable long carId,
    @ApiParam(value = "Updated car object", required = true) @RequestBody @Valid CarDto dto)
    throws RideAustinException {
    Car editedCar = driverCarsService.editCar(driverId, carId, dto);
    return carDtoAssembler.toDto(editedCar);
  }

  @DriverEndpoint
  @WebClientEndpoint
  @ApiOperation("Remove a car")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @DeleteMapping(path = "/{driverId}/cars/{carId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver or admin"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver or car is not found"),
  })
  public void removeCar(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long driverId,
    @ApiParam(value = "Car ID", example = "1") @PathVariable long carId
  ) throws RideAustinException {
    driverCarsService.removeDriverCar(carId, driverId);
  }

}