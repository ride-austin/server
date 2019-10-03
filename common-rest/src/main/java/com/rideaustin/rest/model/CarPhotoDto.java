package com.rideaustin.rest.model;

import com.rideaustin.model.enums.DocumentType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@ApiModel
@AllArgsConstructor
public class CarPhotoDto {

  @ApiModelProperty(required = true)
  private final long id;
  @ApiModelProperty(required = true)
  private final String photoUrl;
  @ApiModelProperty(required = true, allowableValues = "CAR_PHOTO_FRONT,CAR_PHOTO_BACK,CAR_PHOTO_INSIDE,CAR_PHOTO_TRUNK")
  private final DocumentType carPhotoType;

}
