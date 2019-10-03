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

@Entity
@Getter
@Setter
@Table(name = "terms")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Terms extends BaseEntity {

  @Column(name = "mandatory")
  private boolean mandatory;

  @Column(name = "current")
  private boolean current;

  @Column(name = "url")
  private String url;

  @Column(name = "version")
  private String version;

  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "publication_date", nullable = false)
  private Date publicationDate;

  @Column(name = "city_id")
  private Long cityId;

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

