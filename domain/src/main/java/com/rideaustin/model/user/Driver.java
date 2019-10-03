package com.rideaustin.model.user;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.ride.Car;
import com.rideaustin.rest.model.DriverOnboardingInfo;
import com.rideaustin.service.user.DriverTypeUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@ApiModel
@AllArgsConstructor
@Table(name = "drivers")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Driver extends Avatar implements DriverOnboardingInfo {

  @ApiModelProperty
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "agreement_date", nullable = false, updatable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private Date agreementDate;

  @Column(nullable = false)
  @ApiModelProperty(required = true)
  private String ssn;

  @Column(name = "license_number")
  @ApiModelProperty(required = true)
  private String licenseNumber;

  @Column(name = "license_state")
  @ApiModelProperty(required = true)
  private String licenseState;

  @Deprecated
  @JsonIgnore
  @ApiModelProperty(hidden = true)
  @Column(name = "insurance_picture_url")
  private String insurancePictureUrl;

  @Deprecated
  @Temporal(TemporalType.DATE)
  @ApiModelProperty(required = true)
  @Column(name = "insurance_expiry_date")
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private Date insuranceExpiryDate;

  @ApiModelProperty(hidden = true)
  @OneToMany(fetch = FetchType.LAZY, mappedBy = "driver")
  private Set<Car> cars = new HashSet<>();

  @Column(name = "rating")
  @ApiModelProperty(required = true)
  private Double rating = 0D;

  @Column(name = "payoneer_id")
  @ApiModelProperty(hidden = true)
  private String payoneerId;

  @Enumerated(EnumType.STRING)
  @ApiModelProperty(hidden = true)
  @Column(name = "payoneer_status")
  private PayoneerStatus payoneerStatus = PayoneerStatus.INITIAL;

  @Temporal(TemporalType.DATE)
  @ApiModelProperty(hidden = true)
  @Column(name = "activation_date")
  private Date activationDate;

  @Enumerated(EnumType.STRING)
  @ApiModelProperty(hidden = true)
  @Column(name = "city_approval_status")
  private CityApprovalStatus cityApprovalStatus = CityApprovalStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @ApiModelProperty(hidden = true)
  @Column(name = "activation_status")
  private DriverActivationStatus activationStatus = DriverActivationStatus.INACTIVE;

  @Type(type = "text")
  @ApiModelProperty(hidden = true)
  @Column(name = "activation_notes")
  private String activationNotes;

  @Enumerated(EnumType.STRING)
  @ApiModelProperty(hidden = true)
  @Column(name = "onboarding_status")
  private DriverOnboardingStatus onboardingStatus = DriverOnboardingStatus.PENDING;

  @ApiModelProperty(hidden = true)
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "onboarding_pending_since")
  private Date onboardingPendingSince = new Date();

  @JsonIgnore
  @ApiModelProperty(hidden = true)
  @Column(name = "granted_driver_types_bitmask")
  private Integer grantedDriverTypesBitmask;

  @ApiModelProperty(hidden = true)
  @Column(name = "direct_connect_id")
  private String directConnectId;

  @Column(name = "city_id")
  private Long cityId = Constants.DEFAULT_CITY_ID;

  @Column(name = "special_flags")
  @ApiModelProperty(hidden = true)
  private Integer specialFlags = 0;

  @Transient
  @Deprecated
  @ApiModelProperty(required = true)
  private Date licenseExpiryDate;

  @Transient
  @ApiModelProperty(hidden = true)
  private Set<String> grantedDriverTypes = new HashSet<>();

  @Transient
  @ApiModelProperty(hidden = true)
  private String payoneerSignupUrl;

  public Driver() {
    super();
    setActive(false);
  }

  public void initPayoneerFields() {
    setPayoneerId(String.valueOf(getId()));
  }

  @Override
  public AvatarType getType() {
    return AvatarType.DRIVER;
  }

  @PostLoad
  private void onLoad() {
    grantedDriverTypes = new HashSet<>();
    if (grantedDriverTypesBitmask != null) {
      grantedDriverTypes.addAll(DriverTypeUtils.fromBitMask(grantedDriverTypesBitmask));
    }
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


