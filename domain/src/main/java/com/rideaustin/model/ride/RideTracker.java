package com.rideaustin.model.ride;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ride_trackers")
@JsonIgnoreProperties(ignoreUnknown = true)
public class RideTracker {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @Column(name = "ride_id")
  private Long rideId;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "tracked_on")
  private Date trackedOn;

  @Column
  private Double latitude;

  @Column
  private Double longitude;

  @Column
  private Double speed;

  @Column
  private Double heading;

  @Column
  private Double course;

  @Column(name = "distance_travelled")
  private BigDecimal distanceTravelled;

  @Column
  private Long sequence;

  @Column
  private Boolean valid = Boolean.TRUE;

  public RideTracker(Double latitude, Double longitude, Double speed, Double heading, Double course, Long sequence) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.speed = speed;
    this.heading = heading;
    this.course = course;
    this.sequence = sequence;
  }

}
