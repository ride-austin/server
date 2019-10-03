package com.rideaustin.model;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.rideaustin.service.model.DriverRidesReportEntry;

@MappedSuperclass
@JsonSerialize
@JsonIgnoreProperties(ignoreUnknown = true)
@SqlResultSetMappings({
  @SqlResultSetMapping(name = "DriverRidesReportResult", classes = {
    @ConstructorResult(targetClass = DriverRidesReportEntry.class,
      columns = {
        @ColumnResult(name = "driver_id", type = Long.class),
        @ColumnResult(name = "user_id", type = Long.class),
        @ColumnResult(name = "first_name", type = String.class),
        @ColumnResult(name = "last_name", type = String.class),
        @ColumnResult(name = "completed_rides", type = Long.class),
        @ColumnResult(name = "priority_fare_rides", type = Long.class),
        @ColumnResult(name = "distance_travelled", type = BigDecimal.class),
        @ColumnResult(name = "driver_base_payment", type = BigDecimal.class),
        @ColumnResult(name = "tips", type = BigDecimal.class),
        @ColumnResult(name = "priority_fare", type = BigDecimal.class),
        @ColumnResult(name = "cancellation_fee", type = BigDecimal.class),
        @ColumnResult(name = "driver_payment", type = BigDecimal.class),
        @ColumnResult(name = "ra_gross_margin", type = BigDecimal.class),
        @ColumnResult(name = "total_fare", type = BigDecimal.class)
      })
  })
})
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_date", nullable = false, updatable = false)
  private Date createdDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_date", nullable = false)
  private Date updatedDate;

  public BaseEntity() {
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Date getCreatedDate() {
    return createdDate;
  }

  @JsonIgnore
  public void setCreatedDate(Date createdDate) {
    this.createdDate = createdDate;
  }

  public Date getUpdatedDate() {
    return updatedDate;
  }

  @JsonIgnore
  public void setUpdatedDate(Date updatedDate) {
    this.updatedDate = updatedDate;
  }

  @PrePersist
  protected void onCreate() {
    this.updatedDate = this.createdDate = new Date();
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedDate = new Date();
  }

  @Override
  public int hashCode() {
    return Long.valueOf(id).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof BaseEntity)) {
      return false;
    }
    BaseEntity other = (BaseEntity) obj;
    return this.getId() == other.getId();
  }

}
