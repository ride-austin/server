package com.rideaustin.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.quartz.SchedulerException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.jobs.EarningsEmailJob;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.rest.model.MobileDriverDriverDto;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.DriverService;
import com.rideaustin.service.ReferralService;
import com.rideaustin.service.SchedulerService;

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
public class DriversCommon {

  private final CurrentUserService cuSvc;
  private final SchedulerService schedulerService;
  private final ReferralService referralService;
  private final DriverService driverService;

  @DriverEndpoint
  @CheckedTransactional
  @ApiOperation("Send a referral email to invite a new driver")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @PostMapping(path = "/{id}/referAFriendByEmail", produces = APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Provided email is invalid"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send an email")
  })
  public void referAFriendByEmail(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Referred email", required = true) @RequestParam String email,
    @ApiParam(value = "City ID", example = "1") @RequestParam(required = false) Long cityId) throws RideAustinException {
    referralService.referAFriendByEmail(id, email, cityId);
  }

  @DriverEndpoint
  @CheckedTransactional
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @ApiOperation("Send a referral text message to invite a new driver")
  @PostMapping(path = "/{id}/referAFriendBySMS", produces = APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Provided phone number is invalid"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send an SMS")
  })
  public void referAFriendBySMS(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Referred SMS", required = true)@RequestParam String phoneNumber,
    @ApiParam(value = "City ID", example = "1") @RequestParam(required = false) Long cityId) throws RideAustinException {
    referralService.referAFriendBySMS(id, phoneNumber, cityId);
  }

  @WebClientEndpoint
  @CheckedTransactional
  @PostMapping("/{id}/earnings")
  @ApiOperation("Send a earnings statement for a driver")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Failed to schedule"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found")
  })
  public void sendEarningsEmail(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Date to get the earnings statement for", example = "2019-12-31") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
    @ApiParam("List of recipient emails") @RequestParam(required = false) List<String> recipient
  ) throws RideAustinException {

    Driver driver = driverService.findDriver(id, cuSvc.getUser());
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("reportDate", date);
    dataMap.put("driverId", driver.getId());
    User currentUser = cuSvc.getUser();
    if (currentUser.isAdmin()) {
      List<String> resolvedRecipients = recipient;
      if (CollectionUtils.isEmpty(resolvedRecipients)) {
        resolvedRecipients = Collections.singletonList(currentUser.getEmail());
      }
      dataMap.put("recipients", resolvedRecipients);
    }
    try {
      schedulerService.triggerJob(EarningsEmailJob.class, dataMap);
    } catch (SchedulerException e) {
      throw new ServerError(e);
    }
  }

  @DriverEndpoint
  @ApiOperation("Update driver's photo")
  @RolesAllowed({AvatarType.ROLE_ADMIN, AvatarType.ROLE_DRIVER})
  @PostMapping(path = "/{id}/photo", consumes = MULTIPART_FORM_DATA_VALUE, produces = APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "Driver not found"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to upload a photo")
  })
  public MobileDriverDriverDto updateDriverPhoto(
    @ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam("Image file") @RequestPart MultipartFile photoData
  ) throws RideAustinException {
    Driver driver = driverService.findDriver(id, cuSvc.getUser());
    driverService.updateDriverPhoto(driver, photoData);

    return driverService.getCurrentDriverInfo();
  }
}
