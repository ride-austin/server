package com.rideaustin.model;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "password_verification_tokens")
public class PasswordVerificationToken extends BaseEntity {

  @Column(name = "token", nullable = false)
  private String token;
  @Column(name = "email", nullable = false)
  private String email;
  @Column(name = "expires_on", nullable = false)
  private Date expiresOn;

  public PasswordVerificationToken(String email) {
    this.email = email;
    this.token = UUID.randomUUID().toString();
    this.expiresOn = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));
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
