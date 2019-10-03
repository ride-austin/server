package com.rideaustin.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.editors.SafeLongEditor;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DirectConnectDto;
import com.rideaustin.rest.model.Location;
import com.rideaustin.rest.model.MobileDriverDriverDto;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.TermsService;
import com.rideaustin.service.model.DirectConnectDriverDto;
import com.rideaustin.service.ride.DirectConnectService;
import com.rideaustin.utils.LicenseStateValidator;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/drivers")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Drivers {

  private final DriverService driverService;
  private final DirectConnectService directConnectService;
  private final TermsService termsService;

  @InitBinder
  public void prepareBinding(WebDataBinder binder) {
    binder.registerCustomEditor(Long.class, "driverId", new SafeLongEditor());
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Sign up as a driver")
  @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Registration data is invalid"),
    @ApiResponse(code = HttpStatus.SC_CONFLICT, message = "Driver did not accept usage terms"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send signup email")
  })
  public MobileDriverDriverDto addDriver(
    @ApiParam(name = "driver", required = true) @RequestPart(name = "driver") Driver driver,
    @ApiParam(required = true) @RequestPart MultipartFile licenseData,
    @ApiParam(required = true) @Deprecated @RequestPart MultipartFile insuranceData,
    @ApiParam(value = "Terms ID to be accepted", example = "1") @RequestParam(required = false) Long acceptedTermId) throws RideAustinException {

    validateAddDriver(driver);
    final Driver createdDriver = driverService.createDriver(driver, licenseData, insuranceData, acceptedTermId);
    return driverService.getDriverInfo(createdDriver.getId());
  }

  @DriverEndpoint
  @PutMapping("/terms/{termsId}")
  @ApiOperation("Accept usage terms")
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Terms have been already accepted"),
    @ApiResponse(code = HttpStatus.SC_CONFLICT, message = "Terms not found")
  })
  public MobileDriverDriverDto acceptTerm(@ApiParam(value = "Terms ID", example = "1") @PathVariable long termsId) throws RideAustinException {
    Driver currentDriver = driverService.getCurrentDriver();
    termsService.acceptTerms(currentDriver, termsId);
    return driverService.getCurrentDriverInfo();
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Get current driver information")
  @GetMapping(path = "/current", produces = APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Requesting user is not a driver")
  })
  public MobileDriverDriverDto getCurrentDriver() throws RideAustinException {
    return driverService.getCurrentDriverInfo();
  }

  @DriverEndpoint
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation("Assign new Direct Connect ID")
  @GetMapping(path = "/{id}/dcid", produces = APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "New DCID can not be assigned")
  })
  public DirectConnectDto getDirectConnectId(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id) throws RideAustinException {
    return new DirectConnectDto(directConnectService.updateDirectConnectId(id));
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @ApiOperation("Get Direct Connect driver information as a rider")
  @GetMapping(path = "connect/{id}", produces = APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found by DCID")
  })
  public DirectConnectDriverDto getDirectConnectDriver(
    @ApiParam(value = "Driver DCID", example = "1") @PathVariable String id,
    @ApiParam @ModelAttribute Location location) throws NotFoundException {
    return directConnectService.findDriverForDirectConnect(id, location.getLat(), location.getLng());
  }

  private void validateAddDriver(Driver d) throws BadRequestException {
    if (StringUtils.isBlank(d.getSsn())) {
      throw new BadRequestException("SSN is required");
    }
    if (StringUtils.isBlank(d.getLicenseNumber())) {
      throw new BadRequestException("License number is required");
    }
    String licenseState = d.getLicenseState();
    if (StringUtils.isBlank(licenseState)) {
      throw new BadRequestException("License state is required");
    }
    if (!new LicenseStateValidator().isValid(licenseState, null)) {
      throw new BadRequestException("Invalid license state");
    }
    User dUser = d.getUser();
    if (dUser == null) {
      throw new BadRequestException("User is required");
    }
    if (dUser.getAddress() == null ||
      StringUtils.isBlank(dUser.getAddress().getAddress())) {
      throw new BadRequestException("Address is required");
    }
    if (dUser.getDateOfBirth() == null) {
      throw new BadRequestException("Date of birth is required");
    }
  }

}