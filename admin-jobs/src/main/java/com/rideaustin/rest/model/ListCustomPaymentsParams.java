package com.rideaustin.rest.model;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.QCustomPayment;
import com.rideaustin.model.enums.CustomPaymentCategory;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class ListCustomPaymentsParams {

  @ApiModelProperty("Custom payment type")
  private List<CustomPaymentCategory> category;

  @ApiModelProperty(value = "Admin ID", example = "1")
  private Long administratorId;

  @ApiModelProperty("Custom payment description")
  private String description;

  @ApiModelProperty(value = "Driver ID", example = "1")
  private Long driverId;

  @ApiModelProperty("Driver name")
  private String driverName;

  @ApiModelProperty("Creator name")
  private String avatarName;

  @ApiModelProperty("Payment created after date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date createdOnAfter;

  @ApiModelProperty("Payment created before date")
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
  private Date createdOnBefore;

  @ApiModelProperty(value = "City ID", example = "1")
  private Long cityId;

  public BooleanBuilder fill() {
    BooleanBuilder bb = new BooleanBuilder();
    QCustomPayment qCustomPayment = QCustomPayment.customPayment;

    if (StringUtils.isNotEmpty(driverName)) {
      String[] names = StringUtils.split(driverName);
      if (names.length > 1) {
        bb.and(qCustomPayment.driver.user.firstname.containsIgnoreCase(names[0]).and(qCustomPayment.driver.user.lastname.containsIgnoreCase(names[1]))
          .or(qCustomPayment.driver.user.firstname.containsIgnoreCase(names[1]).and(qCustomPayment.driver.user.lastname.containsIgnoreCase(names[0]))));
      } else {
        bb.and(qCustomPayment.driver.user.firstname.containsIgnoreCase(driverName).or(qCustomPayment.driver.user.lastname.containsIgnoreCase(driverName)));
      }
    }

    if (StringUtils.isNotEmpty(avatarName)) {
      String[] names = StringUtils.split(avatarName);
      if (names.length > 1) {
        bb.and(qCustomPayment.creator.user.firstname.containsIgnoreCase(names[0]).and(qCustomPayment.creator.user.lastname.containsIgnoreCase(names[1]))
          .or(qCustomPayment.creator.user.firstname.containsIgnoreCase(names[1]).and(qCustomPayment.creator.user.lastname.containsIgnoreCase(names[0]))));
      } else {
        bb.and(qCustomPayment.creator.user.firstname.containsIgnoreCase(avatarName).or(qCustomPayment.creator.user.lastname.containsIgnoreCase(avatarName)));
      }
    }

    if (CollectionUtils.isNotEmpty(category)) {
      bb.and(qCustomPayment.category.in(category));
    }
    if (!StringUtils.isEmpty(description)) {
      bb.and(qCustomPayment.description.containsIgnoreCase(description));
    }
    if (driverId != null) {
      bb.and(qCustomPayment.driver.id.eq(driverId));
    }
    if (administratorId != null) {
      bb.and(qCustomPayment.creator.id.eq(administratorId));
    }
    if (createdOnAfter != null) {
      bb.and(qCustomPayment.createdDate.goe(createdOnAfter));
    }
    if (createdOnBefore != null) {
      bb.and(qCustomPayment.createdDate.loe(createdOnBefore));
    }
    if (cityId != null) {
      bb.and(qCustomPayment.driver.cityId.eq(cityId));
    }

    return bb;
  }

}
