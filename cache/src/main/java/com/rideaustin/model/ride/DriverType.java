package com.rideaustin.model.ride;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "driver_types")
public class DriverType {

  public static final String DIRECT_CONNECT = "DIRECT_CONNECT";
  public static final String FINGERPRINTED = "FINGERPRINTED";
  public static final String WOMEN_ONLY = "WOMEN_ONLY";
  public static final Integer DIRECT_CONNECT_BITMASK = 2;

  @Id
  @Column(name = "name", unique = true, nullable = false)
  private String name;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created_date", nullable = false, updatable = false)
  private Date createdDate;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated_date", nullable = false)
  private Date updatedDate;

  @Column(name = "enabled")
  @JsonIgnore
  private boolean enabled;

  @Column(name = "description")
  private String description;

  @Column(name = "bitmask")
  @JsonIgnore
  private Integer bitmask;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "driverType")
  @JsonManagedReference
  private Set<CityDriverType> cityDriverTypes;

}