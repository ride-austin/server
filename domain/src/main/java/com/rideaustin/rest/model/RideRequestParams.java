package com.rideaustin.rest.model;

import org.apache.commons.lang3.StringUtils;

import com.rideaustin.model.enums.AvatarType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
public class RideRequestParams {

  @ApiModelProperty(value = "Requested driver type", allowableValues = "DIRECT_CONNECT,WOMAN_ONLY,FINGERPRINTED")
  private String driverType;
  @ApiModelProperty("Apple pay token obtained from Stripe SDK")
  private String applePayToken;
  @ApiModelProperty("Ride comment")
  private String comment;
  @ApiModelProperty("Driver Direct Connect ID, makes sense only for requests with driverType=DIRECT_CONNECT")
  private String directConnectId;
  /**
   * override rider info
   */
  @ApiModelProperty("Overriden rider first name")
  private String riderFirstName;
  @ApiModelProperty("Overriden rider last name")
  private String riderLastName;
  @ApiModelProperty("Overriden rider phone number")
  private String riderPhoneNumber;
  /**
   * API_CLIENT support
   */
  @ApiModelProperty(value = "Requesting avatar type", allowableValues = "RIDER,API_CLIENT")
  private AvatarType avatarType = AvatarType.RIDER;

  @ApiModelProperty(hidden = true)
  public boolean isRiderOverridden() {
    return StringUtils.isNotEmpty(riderFirstName) || StringUtils.isNotEmpty(riderLastName) ||
      StringUtils.isNotEmpty(riderPhoneNumber);
  }
}
