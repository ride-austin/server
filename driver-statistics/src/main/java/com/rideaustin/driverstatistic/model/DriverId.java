package com.rideaustin.driverstatistic.model;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.springframework.data.annotation.PersistenceConstructor;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@Embeddable
@NoArgsConstructor
public class DriverId {

  @Getter
  @Column(name = "driver_id", unique = true, nullable = false)
  private Long id;

  @PersistenceConstructor
  public DriverId(Long id) {
    this.id = id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    DriverId driverId = (DriverId) o;

    return Objects.equals(id, driverId.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : 0;
  }
}
