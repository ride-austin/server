package com.rideaustin.model.reports;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

@Entity
@Immutable
@Table(name = "promocode_usage_report")
public class PromocodeUsageReportEntry {

  @EmbeddedId
  private Id id;
  @Column(name = "first_name", updatable = false, insertable = false)
  private String firstName;
  @Column(name = "last_name", updatable = false, insertable = false)
  private String lastName;
  @Column(name = "email", updatable = false, insertable = false)
  private String email;
  @Column(name = "completed_on", updatable = false, insertable = false)
  private Date completedOn;
  @Column(name = "code_literal", updatable = false, insertable = false)
  private String codeLiteral;

  @Embeddable
  public static class Id implements Serializable {
    @Column(name = "redemption_id")
    private Long redemptionId;
    @Column(name = "rider_id")
    private Long riderId;
    @Column(name = "ride_id")
    private Long rideId;

    public Long getRedemptionId() {
      return redemptionId;
    }

    public void setRedemptionId(Long redemptionId) {
      this.redemptionId = redemptionId;
    }

    public Long getRiderId() {
      return riderId;
    }

    public void setRiderId(Long riderId) {
      this.riderId = riderId;
    }

    public Long getRideId() {
      return rideId;
    }

    public void setRideId(Long rideId) {
      this.rideId = rideId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Id)) {
        return false;
      }
      Id id = (Id) o;
      return Objects.equals(redemptionId, id.redemptionId) &&
        Objects.equals(riderId, id.riderId) &&
        Objects.equals(rideId, id.rideId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(redemptionId, riderId, rideId);
    }
  }

  public Id getId() {
    return id;
  }

  public void setId(Id id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public Date getCompletedOn() {
    return completedOn;
  }

  public void setCompletedOn(Date completedOn) {
    this.completedOn = completedOn;
  }

  public String getCodeLiteral() {
    return codeLiteral;
  }

  public void setCodeLiteral(String codeLiteral) {
    this.codeLiteral = codeLiteral;
  }
}