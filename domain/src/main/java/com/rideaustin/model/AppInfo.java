package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.PlatformType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@ApiModel
@Table(name = "app_infos")
public class AppInfo extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(name = "avatar_type")
  @ApiModelProperty(required = true)
  private AvatarType avatarType;

  @Enumerated(EnumType.STRING)
  @Column(name = "platform_type")
  @ApiModelProperty(required = true)
  private PlatformType platformType;

  @Column(name = "version")
  @ApiModelProperty(required = true)
  private String version;

  @Column(name = "build")
  @ApiModelProperty(required = true)
  private Integer build;

  @ApiModelProperty(required = true)
  @Column(name = "mandatory_upgrade")
  private Boolean mandatoryUpgrade;

  @ApiModelProperty(required = true)
  @Column(name = "user_agent_header")
  private String userAgentHeader;

  @Column(name = "download_url")
  @ApiModelProperty(required = true)
  private String downloadUrl;

  @Column(name = "city_id")
  @ApiModelProperty(required = true, example = "1")
  private Long cityId;

  public void copyFrom(AppInfo info) {
    setAvatarType(info.getAvatarType());
    setPlatformType(info.getPlatformType());
    setVersion(info.getVersion());
    setBuild(info.getBuild());
    setMandatoryUpgrade(info.getMandatoryUpgrade());
    setUserAgentHeader(info.getUserAgentHeader());
    setDownloadUrl(info.getDownloadUrl());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}