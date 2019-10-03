package com.rideaustin.rest;

import static com.rideaustin.Constants.ErrorMessages.EMAIL_NOT_VALID;
import static com.rideaustin.Constants.ErrorMessages.FIRST_NAME_REQUIRED;
import static com.rideaustin.Constants.ErrorMessages.LAST_NAME_REQUIRED;
import static com.rideaustin.Constants.ErrorMessages.PASS_REQUIRED;
import static com.rideaustin.Constants.ErrorMessages.PHONE_NUMBER_REQUIRED;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.Constants;
import com.rideaustin.DriverEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.WebClientEndpoint;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.UpdateUserDto;
import com.rideaustin.service.CityService;
import com.rideaustin.service.CurrentUserService;
import com.rideaustin.service.UserService;
import com.rideaustin.service.promocodes.PromocodeService;
import com.rideaustin.service.user.RiderService;
import com.rideaustin.user.tracking.model.UserTrackData;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Validated
@RestController
@RequestMapping("/rest/users")
@CheckedTransactional
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class Users {

  private final CurrentUserService cuSvc;
  private final CityService cityService;
  private final UserService userService;
  private final RiderService riderService;
  private final PromocodeService promocodeService;

  @RiderEndpoint
  @ApiOperation("Create a new rider account")
  @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Phone number is of invalid format or is forbidden to use"),
    @ApiResponse(code = HttpStatus.SC_UNPROCESSABLE_ENTITY, message = "Provided data is invalid"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to get phone number status from Twilio")
  })
  public Rider post(
    @ApiParam(value = "Email", required = true) @Valid @Email(   message = EMAIL_NOT_VALID,       payload = WhenInvalidReturn.HTTP422.class) @RequestParam String email,
    @ApiParam(value = "First name", required = true) @Valid @NotEmpty(message = FIRST_NAME_REQUIRED,   payload = WhenInvalidReturn.HTTP422.class) @RequestParam String firstname,
    @ApiParam(value = "Last name", required = true) @Valid @NotEmpty(message = LAST_NAME_REQUIRED,    payload = WhenInvalidReturn.HTTP422.class) @RequestParam String lastname,
    @ApiParam(value = "Password, either hashed or plain-text", required = true) @Valid @NotEmpty(message = PASS_REQUIRED,         payload = WhenInvalidReturn.HTTP422.class) @RequestParam String password,
    @ApiParam(value = "Phone number", required = true) @Valid @NotEmpty(message = PHONE_NUMBER_REQUIRED, payload = WhenInvalidReturn.HTTP422.class) @RequestParam String phonenumber,
    @ApiParam("Facebook ID") @RequestParam(required = false) String socialId,
    @ApiParam("Base64-encoded photo image") @RequestParam(value = "data", required = false) String userPhoto,
    @ApiParam("Was the phone number verified") @RequestParam(value = "phonenumberVerified", required = false) Boolean phoneNumberVerified,
    @ApiParam(value = "City ID", example = "1", defaultValue = "1") @RequestParam(defaultValue = "1", required = false) Long cityId,
    HttpServletRequest request) throws RideAustinException {

    long resolvedCity = cityId;
    if (!cityService.getCitiesIds().contains(resolvedCity)) {
      resolvedCity = Constants.DEFAULT_CITY_ID;
    }

    User user = userService.createUser(email, socialId, firstname, lastname, password, phonenumber, userPhoto,
      phoneNumberVerified, request);

    // Create a new rider with this user
    final Rider rider = riderService.createRider(resolvedCity, user);

    try {
      UserTrackData userTrackData = new UserTrackData(request.getParameterMap());
      if (!userTrackData.isEmpty() && StringUtils.isNotEmpty(userTrackData.getPromoCode())) {
        promocodeService.applyPromocode(rider.getId(), userTrackData.getPromoCode(), false);
      }
    } catch (Exception e) {
      log.error("Failed to apply sign-up promocode", e);
    }

    return rider;
  }

  @DriverEndpoint
  @ApiOperation("Get user information")
  @GetMapping(value = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_NOT_FOUND, message = "User not found")
  })
  public User get(@PathVariable long id) throws NotFoundException {
    User currentUser = cuSvc.getUser();
    if (!currentUser.isAdmin()) {
      return currentUser;
    }
    return userService.getUser(id);
  }

  @WebClientEndpoint
  @ApiOperation("Get current user information")
  @GetMapping(value = "current", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK")
  })
  public User getCurrentUser() {
    return cuSvc.getUser();
  }

  @RiderEndpoint
  @ApiOperation("Update user information")
  @PutMapping(value = "{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Phone number is of invalid format or is forbidden to use"),
    @ApiResponse(code = HttpStatus.SC_FORBIDDEN, message = "Updating other user's details is forbidden"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to get phone number status from Twilio")
  })
  public User update(@PathVariable("id") long id, @RequestBody UpdateUserDto user) throws RideAustinException {
    return userService.updateUser(id, user);
  }

  @RiderEndpoint
  @PostMapping(value = "/exists")
  @ApiOperation("Check if the user with requested email and/or phone number already exists")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK. Email/phone number is not registered in the database"),
    @ApiResponse(code = HttpStatus.SC_BAD_REQUEST, message = "Email/phone number is already registered in the database"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to get phone number status from Twilio")
  })
  public void isUserExist(
    @ApiParam("Email") @RequestParam(value = "email", required = false) String email,
    @ApiParam("Phone number") @RequestParam(value = "phoneNumber", required = false) String phoneNumber
  ) throws RideAustinException {
    userService.checkIfUserNameAndPhoneNumberIsAvailable(email, phoneNumber, null);
  }

}