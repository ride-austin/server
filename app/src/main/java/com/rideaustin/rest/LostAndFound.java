package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rideaustin.DriverEndpoint;
import com.rideaustin.RiderEndpoint;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.FoundItemDto;
import com.rideaustin.rest.model.LostItemDto;
import com.rideaustin.service.LostAndFoundService;
import com.rideaustin.service.thirdparty.CommunicationService;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Validated
@RestController
@RequestMapping("/rest/lostandfound")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class LostAndFound {

  private static final String LOST_FOUND_SUCCESS_MESSAGE = "Thank you. We’ve received your message and we will reach out to you as soon as possible.";
  private static final String CONTACT_SUCCESS_MESSAGE = "We are now connecting you to your driver. If your driver doesn't pick up, leave a detailed voicemail describing your item and the best way to contact you.";
  private static final String PHONE_NUMBER_VALIDATION_MESSAGE = "Phone number should start with + sign and contain from 8 to 15 digits";

  private final LostAndFoundService lostAndFoundService;

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @PostMapping(value = "contact", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Contact the driver about the lost item")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK", response = ContactSuccessResponse.class),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to contact the driver", response = String.class)
  })
  public ResponseEntity<Object> contact(
    @ApiParam(value = "ID of the ride while which the item was lost", example = "1", required = true) @RequestParam Long rideId,
    @ApiParam(value = "Phone number", example = "+15125555555", required = true)
    @RequestParam(name = "phone") @Valid
    @Pattern(regexp = "\\+[0-9]{8,15}", message = PHONE_NUMBER_VALIDATION_MESSAGE) String phoneNumber
  ) throws RideAustinException {
    CommunicationService.CallStatus callStatus = lostAndFoundService.initiateCall(rideId, phoneNumber);
    return callStatus == CommunicationService.CallStatus.ERROR ?
      ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(callStatus.getStatus()) :
      ResponseEntity.ok(new ContactSuccessResponse(callStatus.getStatus(), CONTACT_SUCCESS_MESSAGE));
  }

  @RiderEndpoint
  @RolesAllowed(AvatarType.ROLE_RIDER)
  @PostMapping(value = "lost", produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation(value = "Report lost item as a rider", response = LostSuccessMessage.class)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_ACCEPTED, message = "Report accepted"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Ride not found"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_FORBIDDEN, message = "Rider tries to report lost item for someone other's ride")
  })
  public ResponseEntity<LostSuccessMessage> lost(
    @ApiParam(value = "ID of the ride while which the item was lost", example = "1", required = true) @RequestParam Long rideId,
    @ApiParam(value = "Phone number", example = "+15125555555", required = true)
    @RequestParam(name = "phone") @Valid
    @Pattern(regexp = "\\+[0-9]{8,15}", message = PHONE_NUMBER_VALIDATION_MESSAGE) String phoneNumber,
    @ApiParam @ModelAttribute @Valid LostItemDto lostItemDto
  ) throws RideAustinException {
    lostAndFoundService.processLostItem(rideId, lostItemDto.getDescription(), lostItemDto.getDetails(), phoneNumber);
    return ResponseEntity.accepted().body(new LostSuccessMessage(LOST_FOUND_SUCCESS_MESSAGE));
  }

  @DriverEndpoint
  @PostMapping("found")
  @RolesAllowed(AvatarType.ROLE_DRIVER)
  @ApiOperation(value = "Report found item as a driver", response = LostSuccessMessage.class)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_ACCEPTED, message = "Report accepted"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Ride not found"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_FORBIDDEN, message = "Driver tries to report lost item for someone other's ride")
  })
  public ResponseEntity<LostSuccessMessage> found(
    @ApiParam @RequestPart @Valid FoundItemDto item,
    @ApiParam("Found item image file") @RequestPart MultipartFile image) throws RideAustinException {
    lostAndFoundService.processFoundItem(item.getRideId(), item.getFoundOn(), item.getRideDescription(), item.getDetails(),
      item.isSharingContactsAllowed(), image);
    return ResponseEntity.accepted().body(new LostSuccessMessage(LOST_FOUND_SUCCESS_MESSAGE));
  }

  @Getter
  @Setter
  @ApiModel
  @AllArgsConstructor
  static class ContactSuccessResponse {
    @ApiModelProperty(required = true)
    final String status;
    @ApiModelProperty(required = true)
    final String message;
  }

  @Getter
  @Setter
  @ApiModel
  @AllArgsConstructor
  static class LostSuccessMessage {
    @ApiModelProperty(required = true)
    final String message;
  }

}
