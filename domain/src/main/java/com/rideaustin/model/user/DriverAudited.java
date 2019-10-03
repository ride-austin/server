package com.rideaustin.model.user;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;

import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "drivers_aud")
@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
public class DriverAudited {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "revision", unique = true, nullable = false)
  private long revision;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "revision_date", nullable = false, updatable = false)
  private Date revisionDate = new Date();

  @Column(name = "username")
  private String username;

  @Column(name = "id", nullable = false)
  private long id;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "agreement_date", nullable = false, updatable = false)
  private Date agreementDate;

  @Column(nullable = false)
  private String ssn;

  @Column(name = "license_number")
  private String licenseNumber;

  @Column(name = "license_state")
  private String licenseState;

  @Column(name = "rating")
  private Double rating;

  @Column(name = "payoneer_id")
  private String payoneerId;

  @Column(name = "payoneer_status")
  @Enumerated(EnumType.STRING)
  private PayoneerStatus payoneerStatus;

  @Column(name = "activation_date")
  @Temporal(TemporalType.DATE)
  private Date activationDate;

  @Column(name = "city_approval_status")
  @Enumerated(EnumType.STRING)
  private CityApprovalStatus cityApprovalStatus;

  @Column(name = "activation_status")
  @Enumerated(EnumType.STRING)
  private DriverActivationStatus activationStatus;

  @Column(name = "activation_notes")
  @Type(type = "text")
  private String activationNotes;

  @Column(name = "onboarding_status")
  @Enumerated(EnumType.STRING)
  private DriverOnboardingStatus onboardingStatus;

  @Column(name = "onboarding_pending_since")
  @Temporal(TemporalType.TIMESTAMP)
  private Date onboardingPendingSince;

  @Column(name = "granted_driver_types_bitmask")
  private Integer grantedDriverTypesBitmask;

  @Column(name = "city_id")
  private Long cityId;

  @Column(name = "direct_connect_id")
  private String directConnectId;
}


