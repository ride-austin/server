package com.rideaustin.rest;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.DriverEmailHistoryItemDtoAssembler;
import com.rideaustin.assemblers.DriverEmailReminderDtoAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.repo.dsl.DriverEmailReminderDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.DriverEmailHistoryItemDto;
import com.rideaustin.rest.model.DriverEmailReminderDto;
import com.rideaustin.rest.model.DriverEmailReminderParams;
import com.rideaustin.service.DriverEmailReminderService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/rest/drivers")
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class DriverEmailReminders {

  private final DriverEmailReminderDslRepository reminderDslRepository;
  private final DriverEmailReminderService driverEmailReminderService;
  private final DriverEmailReminderDtoAssembler dtoAssembler;
  private final DriverEmailHistoryItemDtoAssembler historyItemDtoAssembler;

  @WebClientEndpoint
  @GetMapping("reminders")
  @ApiOperation("Get a list of available email reminders")
  public List<DriverEmailReminderDto> list() {
    return dtoAssembler.toDto(reminderDslRepository.listReminders());
  }

  @WebClientEndpoint
  @ResponseStatus(HttpStatus.ACCEPTED)
  @ApiOperation("Send a reminder to a driver")
  @PostMapping("{driverId}/reminders/{reminderId}")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_ACCEPTED, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Content parameter is required, but was not provided"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to send email")
  })
  public void sendReminder(@ApiParam(value = "Driver ID", example = "1") @PathVariable Long driverId,
    @ApiParam(value = "Reminder ID", example = "1") @PathVariable Long reminderId,
    @ApiParam @ModelAttribute DriverEmailReminderParams params) throws RideAustinException {
    try {
      driverEmailReminderService.sendReminder(driverId, reminderId, params.getContent(), params.getSubject());
    } catch (IllegalArgumentException e) { // arguments validation failed
      log.error("Failed to send reminder", e);
      throw new BadRequestException(e.getMessage());
    }
  }

  @WebClientEndpoint
  @RolesAllowed(AvatarType.ROLE_ADMIN)
  @ApiOperation("Get history of reminders sent to a driver")
  public List<DriverEmailHistoryItemDto> history(@ApiParam(value = "Driver ID", example = "1") @PathVariable Long driverId) {
    return historyItemDtoAssembler.toDto(driverEmailReminderService.getHistory(driverId));
  }

  @WebClientEndpoint
  @GetMapping("reminders/history/{reminderHistoryId}")
  @ApiOperation("Get content of an already sent reminder")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "History item not found"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to render history item template")
  })
  public String reminderHistoryContent(@ApiParam(value = "Reminder history item ID", example = "1") @PathVariable Long reminderHistoryId) throws RideAustinException {
    return driverEmailReminderService.getReminderHistoryContent(reminderHistoryId);
  }

  @WebClientEndpoint
  @ApiOperation("Get reminder content")
  @GetMapping("{driverId}/reminders/{reminderId}")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Driver or reminder item is not found"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Failed to render history item template")
  })
  public String reminderContent(@ApiParam(value = "Driver ID", example = "1") @PathVariable Long driverId,
    @ApiParam(value = "Reminder ID", example = "1") @PathVariable Long reminderId) throws RideAustinException {
    return driverEmailReminderService.getReminderContent(reminderId, driverId);
  }
}
