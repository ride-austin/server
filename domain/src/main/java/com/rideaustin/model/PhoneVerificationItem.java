package com.rideaustin.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "phone_verification_items")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhoneVerificationItem extends BaseEntity {

  @Column(name = "phone_number")
  private String phoneNumber;

  @Column(name = "auth_token")
  private String authToken;

  @Column(name = "verification_code")
  private String verificationCode;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "verified_on")
  private Date verifiedOn;

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

