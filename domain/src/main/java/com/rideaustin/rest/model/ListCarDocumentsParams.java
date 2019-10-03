package com.rideaustin.rest.model;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.QDocument;
import com.rideaustin.model.enums.DocumentType;
import com.rideaustin.model.ride.QCar;
import com.rideaustin.model.user.QDriver;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel
public class ListCarDocumentsParams {

  private static final QDriver qDriver = QDriver.driver;
  private static final QDocument qDocument = QDocument.document;
  private static final QCar qCar = QCar.car;

  @Getter
  @Setter
  @ApiModelProperty(value = "Driver ID", example = "1")
  private Long driverId;
  @Getter
  @Setter
  @ApiModelProperty(value = "Car ID", example = "1")
  private Long carId;
  @Getter
  @Setter
  @ApiModelProperty(value = "City ID", example = "1")
  private Long cityId;
  @Getter
  @Setter
  @ApiModelProperty(value = "Document type", allowableValues = "INSURANCE,CAR_PHOTO_FRONT,CAR_PHOTO_INSIDE,CAR_PHOTO_TRUNK,CAR_PHOTO_BACK,CAR_STICKER")
  private DocumentType documentType;

  public void fill(BooleanBuilder builder) {
    builder.and(qDriver.id.eq(driverId));
    builder.and(qCar.id.eq(carId));
    if (documentType != null) {
      builder.and(qDocument.documentType.eq(documentType));
    }
    if (cityId != null) {
      builder.and(qDocument.cityId.eq(cityId));
    }

  }

}
