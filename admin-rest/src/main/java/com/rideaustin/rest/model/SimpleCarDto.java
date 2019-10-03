package com.rideaustin.rest.model;

import java.util.Map;
import java.util.Set;

import com.rideaustin.model.enums.CarInspectionStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.enums.DocumentType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class SimpleCarDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final String color;
  @ApiModelProperty(required = true)
  private final String license;
  @ApiModelProperty(required = true)
  private final String make;
  @ApiModelProperty(required = true)
  private final String model;
  @ApiModelProperty(required = true)
  private final long driverId;
  @ApiModelProperty(required = true)
  private final String year;
  @ApiModelProperty(required = true)
  private final Set<String> categories;
  @ApiModelProperty(required = true)
  private final CarInspectionStatus inspectionStatus;
  @ApiModelProperty(required = true)
  private final String insurancePhotoUrl;
  @ApiModelProperty(required = true)
  private final DocumentStatus insuranceStatus;
  @ApiModelProperty(required = true)
  private final DocumentStatus inspectionStickerStatus;
  @ApiModelProperty(value = "Status of car photos", required = true)
  private final Map<DocumentType, DocumentStatus> carPhotosStatus;

  private SimpleCarDto(Long id, String color, String license, String make, String model, Long driverId, String year,
    Set<String> categories, CarInspectionStatus inspectionStatus, String insurancePhotoUrl, DocumentStatus insuranceStatus,
    DocumentStatus inspectionStickerStatus, Map<DocumentType, DocumentStatus> carPhotosStatus) {
    this.id = id;
    this.color = color;
    this.license = license;
    this.make = make;
    this.model = model;
    this.driverId = driverId;
    this.year = year;
    this.categories = categories;
    this.inspectionStatus = inspectionStatus;
    this.insurancePhotoUrl = insurancePhotoUrl;
    this.insuranceStatus = insuranceStatus;
    this.inspectionStickerStatus = inspectionStickerStatus;
    this.carPhotosStatus = carPhotosStatus;
  }

  public static class Builder {
    private Long id;
    private String color;
    private String license;
    private String make;
    private String model;
    private Long driverId;
    private String year;
    private Set<String> categories;
    private CarInspectionStatus inspectionStatus;
    private String insurancePhotoUrl;
    private DocumentStatus insuranceStatus;
    private DocumentStatus inspectionStickerStatus;
    private Map<DocumentType, DocumentStatus> carPhotosStatus;

    public Builder id(Long id) {
      this.id = id;
      return this;
    }

    public Builder color(String color) {
      this.color = color;
      return this;
    }

    public Builder license(String license) {
      this.license = license;
      return this;
    }

    public Builder make(String make) {
      this.make = make;
      return this;
    }

    public Builder model(String model) {
      this.model = model;
      return this;
    }

    public Builder driverId(Long driverId) {
      this.driverId = driverId;
      return this;
    }

    public Builder year(String year) {
      this.year = year;
      return this;
    }

    public Builder categories(Set<String> categories) {
      this.categories = categories;
      return this;
    }

    public Builder inspectionStatus(CarInspectionStatus inspectionStatus) {
      this.inspectionStatus = inspectionStatus;
      return this;
    }

    public Builder insurancePhotoUrl(String insurancePhotoUrl) {
      this.insurancePhotoUrl = insurancePhotoUrl;
      return this;
    }

    public Builder insuranceStatus(DocumentStatus insuranceStatus) {
      this.insuranceStatus = insuranceStatus;
      return this;
    }

    public Builder carPhotosStatus(Map<DocumentType, DocumentStatus> carPhotosStatus) {
      this.carPhotosStatus = carPhotosStatus;
      return this;
    }

    public Builder inspectionStickerStatus(DocumentStatus inspectionStickerStatus) {
      this.inspectionStickerStatus = inspectionStickerStatus;
      return this;
    }

    public SimpleCarDto build() {
      return new SimpleCarDto(id, color, license ,make, model, driverId, year, categories, inspectionStatus, insurancePhotoUrl,
        insuranceStatus, inspectionStickerStatus, carPhotosStatus);
    }
  }
}
