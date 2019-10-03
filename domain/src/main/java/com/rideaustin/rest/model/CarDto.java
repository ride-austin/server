package com.rideaustin.rest.model;

import java.util.Date;
import java.util.Set;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.enums.CarInspectionStatus;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CarDto {
  @ApiModelProperty(required = true)
  private long id;
  @ApiModelProperty(required = true)
  @NotEmpty(message = "Color is required!")
  private String color;
  @ApiModelProperty(required = true)
  @NotEmpty(message = "License is required")
  private String license;
  @ApiModelProperty(required = true)
  @NotEmpty(message = "Make is required")
  private String make;
  @ApiModelProperty(required = true)
  @NotEmpty(message = "Model is required")
  private String model;
  @ApiModelProperty(required = true)
  @NotEmpty(message = "Year is required")
  private String year;
  @ApiModelProperty(required = true)
  private Date insuranceExpiryDate;
  @ApiModelProperty(required = true)
  private String insurancePictureUrl;
  @ApiModelProperty(required = true)
  private Boolean selected;
  @ApiModelProperty(required = true)
  private CarInspectionStatus inspectionStatus;
  @ApiModelProperty
  private String inspectionNotes;
  @ApiModelProperty(required = true)
  private Boolean removed;
  @ApiModelProperty(required = true)
  private Set<String> carCategories;
  @ApiModelProperty(required = true)
  private String photoUrl;

  public CarDto(long id, String color, String license, String make, String model, String year,
    Date insuranceExpiryDate, String insurancePictureUrl, Boolean selected, CarInspectionStatus inspectionStatus,
    String inspectionNotes, Boolean removed, Set<String> carCategories, String photoUrl) {
    this.id = id;
    this.color = color;
    this.license = license;
    this.make = make;
    this.model = model;
    this.year = year;
    this.insuranceExpiryDate = insuranceExpiryDate;
    this.insurancePictureUrl = insurancePictureUrl;
    this.selected = selected;
    this.inspectionStatus = inspectionStatus;
    this.inspectionNotes = inspectionNotes;
    this.removed = removed;
    this.carCategories = carCategories;
    this.photoUrl = photoUrl;
  }

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
  public Date getInsuranceExpiryDate() {
    return insuranceExpiryDate;
  }

  @ApiModelProperty
  @JsonProperty("photoURL")
  public String getPhotoUrl0() {
    return photoUrl;
  }

}
