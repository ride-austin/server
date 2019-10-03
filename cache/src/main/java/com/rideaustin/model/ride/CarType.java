package com.rideaustin.model.ride;

import java.util.HashMap;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.rideaustin.model.JsonConfigurable;
import com.rideaustin.service.ride.CarTypeRequestHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "car_types")
public class CarType implements JsonConfigurable<CarType.Configuration> {

  @Id
  @Column(name = "car_category", unique = true, nullable = false)
  private String carCategory;

  @Column(name = "title")
  private String title;

  @Column(name = "description")
  private String description;

  @Column(name = "icon_url")
  private String iconUrl;

  @Column(name = "plain_icon_url")
  private String plainIconUrl;

  @Column(name = "map_icon_url")
  private String mapIconUrl;

  @Column(name = "full_icon_url")
  private String fullIconUrl;

  @Column(name = "selected_icon_url")
  private String selectedIconUrl;

  @Column(name = "unselected_icon_url")
  private String unselectedIconUrl;

  @Column(name = "selected_female_icon_url")
  private String selectedFemaleIconUrl;

  @Column(name = "configuration")
  private String configuration;

  @Column(name = "max_persons")
  private Integer maxPersons;

  @Column(name = "order")
  private Integer order;

  @Column(name = "bitmask")
  @JsonIgnore
  private Integer bitmask;

  @Column(name = "active")
  @JsonIgnore
  private Boolean active;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "carType")
  @JsonManagedReference
  private Set<CityCarType> cityCarTypes;

  @Override
  public Configuration getDefaultConfiguration() {
    return new Configuration();
  }

  @Override
  public Class<Configuration> getConfigurationClass() {
    return Configuration.class;
  }

  public static class Configuration extends HashMap<String, Object> {

    public Configuration() {
      super();
    }

    public boolean isSkipRideAuthorization() {
      return (boolean) getOrDefault("skipRideAuthorization", false);
    }

    public boolean isTippingEnabled() {
      return !((boolean) getOrDefault("disableTipping", false));
    }

    public Class<CarTypeRequestHandler> getRequestHandlerClass() {
      String className = (String) get("requestHandlerClass");
      if (className != null) {
        try {
          return (Class<CarTypeRequestHandler>) Class.forName(className);
        } catch (Exception e) {
          log.error("Failed to get handler class", e);
          return null;
        }
      }
      return null;
    }
  }
}
