package com.rideaustin.rest.model;

import org.apache.commons.lang3.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.rideaustin.model.user.QUser;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel
class ListAvatarsParams {

  @ApiModelProperty("Name")
  private String name;
  @ApiModelProperty("Email")
  private String email;
  @ApiModelProperty("Is profile active")
  private Boolean active;
  @ApiModelProperty("Is profile enabled")
  private Boolean enabled;

  void fill(BooleanBuilder builder, QUser qUser) {
    if (StringUtils.isNotEmpty(name)) {
      builder.and(qUser.firstname.containsIgnoreCase(name).or(qUser.lastname.containsIgnoreCase(name)));
      String[] names = StringUtils.split(name);
      if (names.length > 1) {
        builder.or(qUser.firstname.containsIgnoreCase(names[0]).and(qUser.lastname.containsIgnoreCase(names[1]))
          .or(qUser.firstname.containsIgnoreCase(names[1]).and(qUser.lastname.containsIgnoreCase(names[0]))));
      } else {
        builder.or(qUser.firstname.containsIgnoreCase(name).or(qUser.lastname.containsIgnoreCase(name)));
      }
    }
    if (StringUtils.isNotEmpty(email)) {
      builder.and(qUser.email.containsIgnoreCase(email));
    }
    if (enabled != null) {
      builder.and(qUser.userEnabled.eq(enabled));
    }
  }

}
