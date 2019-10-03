package com.rideaustin.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.rideaustin.filter.ClientType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "configuration_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"configurationValue"})
public class ConfigurationItem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", unique = true, nullable = false)
  private long id;

  @Column(name = "city_id")
  private Long cityId;

  @Column(name = "client_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private ClientType clientType;

  @Column(name = "configuration_key", nullable = false)
  private String configurationKey;

  @Column(name = "configuration_value", nullable = false)
  private String configurationValue;

  @Column(name = "is_default", nullable = false)
  private boolean isDefault = true;

  @Column(name = "environment")
  private String environment;

  @Transient
  private Object configurationObject;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    ConfigurationItem that = (ConfigurationItem) o;

    return id == that.id;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
