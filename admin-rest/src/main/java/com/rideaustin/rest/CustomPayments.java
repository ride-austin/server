package com.rideaustin.rest;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.quartz.SchedulerException;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.WebClientEndpoint;
import com.rideaustin.assemblers.CustomPaymentDtoAssembler;
import com.rideaustin.jobs.export.CustomPaymentExportJob;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CustomPaymentCategory;
import com.rideaustin.rest.editors.SafeLongEditor;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.CustomPaymentDto;
import com.rideaustin.rest.model.ListCustomPaymentsParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.CustomPaymentService;
import com.rideaustin.service.SchedulerService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CustomPayments {

  private final CustomPaymentService customPaymentService;
  private final CustomPaymentDtoAssembler customPaymentDtoAssembler;
  private final SchedulerService schedulerService;

  @InitBinder
  public void prepareBinding(WebDataBinder binder) {
    binder.registerCustomEditor(Long.class, "administratorId", new SafeLongEditor());
    binder.registerCustomEditor(Long.class, "driverId", new SafeLongEditor());
  }

  @WebClientEndpoint
  @ApiOperation(value = "List all custom payments complying with provided search criteria", response = CustomPaymentDto.class, responseContainer = "List")
  @GetMapping(path = "/rest/custompayment", produces = MediaType.APPLICATION_JSON_VALUE)
  public Page<CustomPaymentDto> listOtherPayments(
    @ApiParam @ModelAttribute ListCustomPaymentsParams params, @ApiParam @ModelAttribute PagingParams paging) {
    return customPaymentService.listOtherPayments(params, paging).map(customPaymentDtoAssembler);
  }

  @ApiOperation("Get one custom payment object")
  @GetMapping(path = "/rest/custompayment/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public CustomPaymentDto getCustomPayment(@ApiParam(value = "Custom payment ID", example = "1") @PathVariable long id) {
    return customPaymentDtoAssembler.toDto(customPaymentService.findById(id));
  }

  @WebClientEndpoint
  @ApiOperation("Create a new custom payment for a driver")
  @PostMapping(path = "/rest/drivers/{id}/custompayment", produces = APPLICATION_JSON_VALUE)
  public CustomPaymentDto createOtherPayment(@ApiParam(value = "Driver ID", example = "1") @PathVariable long id,
    @ApiParam(value = "Type of custom payment", required = true) @RequestParam CustomPaymentCategory type,
    @ApiParam(value = "Custom payment description", required = true) @RequestParam String description,
    @ApiParam(value = "Custom payment amount", required = true, example = "10.0") @RequestParam Double value,
    @ApiParam(value = "Custom payment date", required = true) @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Instant paymentDate) throws RideAustinException {
    return customPaymentDtoAssembler.toDto(customPaymentService.createOtherPayment(type, description, id, value, Date.from(paymentDate)));
  }

  @WebClientEndpoint
  @ApiOperation("Trigger a custom payment report job")
  @ApiResponses({
    @ApiResponse(code = HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = HttpStatus.SC_INTERNAL_SERVER_ERROR, message = "Custom payment report job failed to be scheduled")
  })
  @GetMapping(path = "/rest/custompayment/report", produces = MediaType.APPLICATION_JSON_VALUE)
  public void exportPayments(
    @ApiParam(value = "Include payments created after", required = true) @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createAfter,
    @ApiParam(value = "Include payments created before", required = true)@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createBefore,
    @ApiParam("Include payments for date") @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Instant paymentDate,
    @ApiParam(value = "City ID", example = "1", defaultValue = "1", required = true) @RequestParam(defaultValue = "1") Long cityId,
    @ApiParam(value = "Comma-separated list of recipient email addresses", required = true) @RequestParam List<String> recipient
  ) throws SchedulerException {
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("createAfter", createAfter);
    dataMap.put("createBefore", createBefore);
    dataMap.put("paymentDate", paymentDate);
    dataMap.put("cityId", cityId);
    dataMap.put("recipients", recipient);

    schedulerService.triggerJob(CustomPaymentExportJob.class, dataMap);
  }

}