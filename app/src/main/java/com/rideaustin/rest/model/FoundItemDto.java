package com.rideaustin.rest.model;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
public class FoundItemDto {

  @NotNull(message = "Found on may not be null")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  @ApiModelProperty(value = "Date the item is found on", required = true)
  private Date foundOn;

  @NotEmpty(message = "Description may not be empty")
  @ApiModelProperty(value = "Ride description", required = true)
  @Length(max = 2000, message = "Description may not exceed 2000 characters")
  private String rideDescription;

  @NotNull(message = "You must either confirm or decline sharing your phone number")
  @ApiModelProperty(value = "Does driver allow to share phone number with the rider", required = true)
  private boolean sharingContactsAllowed;

  @NotEmpty(message = "Details may not be empty")
  @Length(max = 2000, message = "Details may not exceed 2000 characters")
  @ApiModelProperty(value = "Description of the found item", required = true)
  private String details;

  @ApiModelProperty("Ride ID")
  private Long rideId;

}
