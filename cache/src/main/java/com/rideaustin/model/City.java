package com.rideaustin.model;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.enums.CityEmailType;
import com.rideaustin.model.surgepricing.AreaGeometry;

import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "cities")
@JsonIgnoreProperties(ignoreUnknown = true)
@EqualsAndHashCode(callSuper = true)
public class City extends BaseEntity {

  @Column(name = "name")
  @ApiModelProperty(required = true)
  private String name;

  @Column(name = "app_name")
  @ApiModelProperty(required = true)
  private String appName;

  @Column(name = "enabled")
  @ApiModelProperty(required = true)
  private boolean enabled;

  @Column(name = "bitmask")
  @JsonIgnore
  @ApiModelProperty(required = true, hidden = true)
  private Integer bitmask;

  @Column(name = "logo_url")
  @ApiModelProperty(required = true)
  private String logoUrl;

  @Column(name = "logo_url_dark")
  @ApiModelProperty(required = true)
  private String logoUrlDark;

  @Column(name = "office")
  @ApiModelProperty(required = true)
  private String office;

  @Column(name = "contact_email")
  @ApiModelProperty(required = true)
  private String contactEmail;

  @Column(name = "support_email")
  @ApiModelProperty(required = true)
  private String supportEmail;

  @Column(name = "documents_email")
  @ApiModelProperty(required = true)
  private String documentsEmail;

  @Column(name = "drivers_email")
  @ApiModelProperty(required = true)
  private String driversEmail;

  @Column(name = "onboarding_email")
  @ApiModelProperty(required = true)
  private String onboardingEmail;

  @Column(name = "play_store_link")
  @ApiModelProperty(required = true)
  private String playStoreLink;

  @Column(name = "app_store_link")
  @ApiModelProperty(required = true)
  private String appStoreLink;

  @Column(name = "page_url")
  @ApiModelProperty(required = true)
  private String pageUrl;

  @ApiModelProperty(required = true)
  @JoinColumn(name = "area_geometry_id")
  @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  private AreaGeometry areaGeometry;

  @Transient
  private Map<CityEmailType, String> emailAddresses;

  @ApiModelProperty
  @Column(name = "unsubscribe_link")
  private String unsubscribeLink;

  @ApiModelProperty
  @Column(name = "update_preferences_link")
  private String updatePreferencesLink;

  @PostLoad
  public void onLoad() {
    emailAddresses = ImmutableMap.of(
      CityEmailType.CONTACT, contactEmail,
      CityEmailType.DOCUMENTS, documentsEmail,
      CityEmailType.DRIVERS, driversEmail,
      CityEmailType.ONBOARDING, onboardingEmail,
      CityEmailType.SUPPORT, supportEmail
    );
  }

  @JsonIgnore
  public Map<CityEmailType, String> getEmailAddresses() {
    return emailAddresses;
  }

}

