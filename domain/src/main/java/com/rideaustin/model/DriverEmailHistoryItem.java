package com.rideaustin.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.rideaustin.model.user.DriverEmailReminder;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "driver_email_history")
public class DriverEmailHistoryItem extends BaseEntity {

  @Column(name = "actor")
  private String actor;
  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
  @JoinColumn(name = "communication_type_id")
  private DriverEmailReminder reminder;
  @Column(name = "driver_id")
  private Long driverId;
  @Column(name = "content")
  @Type(type = "text")
  private String content;

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
