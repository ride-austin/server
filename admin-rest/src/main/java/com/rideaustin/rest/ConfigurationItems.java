package com.rideaustin.rest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.rideaustin.Constants.City;
import com.rideaustin.assemblers.ConfigurationItemDtoAssembler;
import com.rideaustin.clients.configuration.ConfigurationItemNotFoundException;
import com.rideaustin.clients.configuration.ConfigurationItemService;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.ConfigurationItem;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.rest.model.ConfigurationItemDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RolesAllowed(AvatarType.ROLE_ADMIN)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
@RequestMapping(path = "/rest/configs/items", produces = MediaType.APPLICATION_JSON_VALUE)
public class ConfigurationItems {

  private final ConfigurationItemService configurationItemService;
  private final ConfigurationItemDtoAssembler assembler;

  @GetMapping
  @ApiOperation("Get list of db-stored configuration items as an admin")
  public List<ConfigurationItemDto> get() {

    List<ConfigurationItem> items = configurationItemService.findAll();

    return items.stream().map(assembler::toDto).collect(Collectors.toList());
  }

  @PostMapping
  @ApiOperation("Create a new configuration item")
  public ConfigurationItemDto post(@ApiParam("New configuration item") @Valid @RequestBody CreateRequest createRequest) {
    ConfigurationItem item = configurationItemService.create(
      City.getByCityId(createRequest.cityId),
      ClientType.valueOf(createRequest.clientType),
      createRequest.configurationKey,
      createRequest.configurationValue,
      createRequest.isDefault);
    return assembler.toDto(item);
  }

  @GetMapping("/{id}")
  @ApiOperation("Get a single configuration item object")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Configuration item not found")
  })
  public ConfigurationItemDto get(@ApiParam(value = "Configuration item ID", example = "1") @PathVariable Long id) throws ConfigurationItemNotFoundException {
    ConfigurationItem item = configurationItemService.findOne(id);
    return assembler.toDto(item);
  }

  @PutMapping("/{id}")
  @ApiOperation("Update a configuration item")
  @ApiResponses({
    @ApiResponse(code = org.apache.http.HttpStatus.SC_OK, message = "OK"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_NOT_FOUND, message = "Configuration item not found"),
    @ApiResponse(code = org.apache.http.HttpStatus.SC_BAD_REQUEST, message = "Request payload is invalid")
  })
  public ConfigurationItemDto put(
    @ApiParam(value = "Configuration item ID", example = "1") @PathVariable Long id,
    @ApiParam("Updated configuration value") @Valid @RequestBody UpdateRequest value) throws ConfigurationItemNotFoundException {
    ConfigurationItem item = configurationItemService.findOne(id);
    configurationItemService.update(item, value.configurationValue);
    return assembler.toDto(configurationItemService.findOne(id));
  }

  @DeleteMapping("/{id}")
  @ApiOperation("Remove existing configuration item")
  public void delete(@ApiParam(value = "Configuration item ID", example = "1") @PathVariable Long id) {
    configurationItemService.remove(id);
  }

  @ResponseBody
  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handle(ConfigurationItemNotFoundException e) {
    log.debug(e.getMessage(), e);
    return ResponseEntity.badRequest().body(e.getMessage());
  }

  @ResponseBody
  @ExceptionHandler
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<String> handle(HttpMessageNotReadableException e) {
    log.debug(e.getMessage(), e);

    if (e.getRootCause() instanceof JsonMappingException) {
      return ResponseEntity.badRequest().body("invalid configurationValue json");
    }
    return ResponseEntity.badRequest().body("invalid request body");
  }

  @ApiModel
  private static class CreateRequest {
    @NotNull
    @JsonProperty
    @ApiModelProperty(required = true, example = "1")
    Long cityId;

    @NotEmpty
    @JsonProperty
    @ApiModelProperty(required = true)
    String clientType;

    @NotNull
    @JsonProperty
    @ApiModelProperty(required = true)
    Boolean isDefault;

    @NotEmpty
    @JsonProperty
    @ApiModelProperty(required = true)
    String configurationKey;

    @NotNull
    @JsonProperty
    @ApiModelProperty(required = true)
    Map<String, Object> configurationValue;

    @JsonCreator
    private CreateRequest(
      @JsonProperty("cityId") Long cityId,
      @JsonProperty("clientType") String clientType,
      @JsonProperty("default") Boolean isDefault,
      @JsonProperty("configurationKey") String configurationKey,
      @JsonProperty("configurationValue") Map<String, Object> configurationValue) {
      this.cityId = cityId;
      this.clientType = clientType;
      this.isDefault = isDefault;
      this.configurationKey = configurationKey;
      this.configurationValue = configurationValue;
    }

  }

  @ApiModel
  private static class UpdateRequest {

    @NotNull
    @JsonProperty
    @ApiModelProperty(required = true)
    Map<String, Object> configurationValue;

    @JsonCreator
    private UpdateRequest(
      @JsonProperty("configurationValue") Map<String, Object> configurationValue) {
      this.configurationValue = configurationValue;
    }
  }

}
