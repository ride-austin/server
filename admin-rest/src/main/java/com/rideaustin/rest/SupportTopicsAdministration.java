package com.rideaustin.rest;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rideaustin.CheckedTransactional;
import com.rideaustin.assemblers.SupportTopicAssembler;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.exception.NotFoundException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.SupportTopicDto;
import com.rideaustin.service.SupportTopicService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@CheckedTransactional
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequestMapping("/rest/supporttopics")
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class SupportTopicsAdministration {

  private final SupportTopicService supportTopicService;
  private final SupportTopicAssembler assembler;

  @GetMapping("/{supportTopicId}")
  @ApiOperation("Get support topic object as an administrator")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Support topic not found")
  })
  public SupportTopicDto getSupportTopic(
    @ApiParam(value = "Support topic ID", example = "1") @PathVariable Long supportTopicId
  ) throws NotFoundException {
    return assembler.toDto(supportTopicService.getSupportTopic(supportTopicId));
  }

  @PutMapping(value = "/{supportTopicId}",
    consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiOperation("Update support topic")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided data invalid"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Support topic not found")
  })
  public SupportTopicDto updateSupportTopic(
    @ApiParam(value = "Support topic ID", example = "1") @PathVariable Long supportTopicId,
    @ApiParam("Support topic object") @Valid @RequestBody SupportTopicDto supportTopicDto
  ) throws RideAustinException {
    return assembler.toDto(supportTopicService.updateSupportTopic(supportTopicId, assembler.toDs(supportTopicDto)));
  }

  @ApiOperation("Create new support topic")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Provided data invalid")
  })
  public SupportTopicDto createSupportTopic(
    @ApiParam("Support topic object") @Valid @RequestBody SupportTopicDto supportTopicDto
  ) throws RideAustinException {
    return assembler.toDto(supportTopicService.createSupportTopic(assembler.toDs(supportTopicDto)));
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @ApiOperation("Remove a support topic")
  @DeleteMapping(value = "/{supportTopicId}")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NO_CONTENT, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Support topic not found")
  })
  public void removeSupportTopic(
    @ApiParam(value = "Support topic ID", example = "1") @PathVariable Long supportTopicId
  ) throws NotFoundException {
    supportTopicService.removeSupportTopic(supportTopicId);
  }
}
