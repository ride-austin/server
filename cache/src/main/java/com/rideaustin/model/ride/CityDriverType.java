package com.rideaustin.model.ride;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rideaustin.model.BaseEntity;
import com.rideaustin.model.JsonConfigurable;
import com.rideaustin.model.user.Gender;
import com.rideaustin.service.user.CarTypesUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "city_driver_types")
public class CityDriverType extends BaseEntity implements JsonConfigurable<CityDriverType.Configuration> {

  @Column(name = "city_id")
  private Long cityId;

  @ManyToOne(fetch = FetchType.EAGER, optional = false)
  @JoinColumn(name = "driver_type")
  @JsonBackReference
  private DriverType driverType;

  @Column(name = "car_categories_bitmask")
  @JsonIgnore
  private int carCategoriesBitmask = 1;

  @Transient
  private Set<String> availableInCategories = new HashSet<>();

  @Column(name = "enabled")
  @JsonIgnore
  private boolean enabled;

  @Column(name = "bitmask")
  @JsonIgnore
  private Integer bitmask;

  @Column(name = "configuration")
  private String configuration;

  @Column(name = "configuration_class")
  private Class<? extends Configuration> configurationClass;

  @PostLoad
  private void onLoad() {
    availableInCategories = new HashSet<>();
    availableInCategories.addAll(CarTypesUtils.fromBitMask(carCategoriesBitmask));
  }

  @Override
  public Configuration getDefaultConfiguration() {
    return new DefaultDriverTypeConfiguration();
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

  @ApiModel
  public interface Configuration {

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    boolean isPenalizeDeclinedRides();

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    String getSearchHandlerClass();

    @JsonProperty
    void setEligibleCategories(Set<String> eligibleCategories);

    @ApiModelProperty(required = true)
    Set<String> getEligibleCategories();

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    boolean isCityValidationRequired();

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    boolean isExclusive();

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    boolean shouldResetOnRedispatch();

    @JsonIgnore
    @ApiModelProperty(hidden = true)
    boolean isVisibleToRider();
  }

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class DefaultDriverTypeConfiguration implements Configuration {

    private boolean penalizeDeclinedRides;
    private Set<String> eligibleCategories;
    private String searchHandlerClass;
    private boolean cityValidationRequired = true;
    private boolean exclusive = true;
    private boolean shouldResetOnRedispatch = false;
    private boolean visibleToRider = true;

    @JsonProperty
    public void setPenalizeDeclinedRides(boolean penalizeDeclinedRides) {
      this.penalizeDeclinedRides = penalizeDeclinedRides;
    }

    @Override
    @JsonProperty
    public void setEligibleCategories(Set<String> eligibleCategories) {
      this.eligibleCategories = eligibleCategories;
    }

    @JsonProperty
    public void setSearchHandlerClass(String searchHandlerClass) {
      this.searchHandlerClass = searchHandlerClass;
    }

    @JsonProperty
    public void setCityValidationRequired(boolean cityValidationRequired) {
      this.cityValidationRequired = cityValidationRequired;
    }

    @JsonProperty
    public void setExclusive(boolean exclusive) {
      this.exclusive = exclusive;
    }

    @Override
    public boolean shouldResetOnRedispatch() {
      return isShouldResetOnRedispatch();
    }

    @JsonProperty("shouldResetOnRedispatch")
    public void setShouldResetOnRedispatch(boolean shouldResetOnRedispatch) {
      this.shouldResetOnRedispatch = shouldResetOnRedispatch;
    }

    @JsonProperty("visibleToRider")
    public void setVisibleToRider(boolean visibleToRider) {
      this.visibleToRider = visibleToRider;
    }
  }

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class WomanOnlyConfiguration extends DefaultDriverTypeConfiguration {

    private String displaySubtitle;
    private String displayTitle;
    private Set<Gender> eligibleGenders;
    private Alert ineligibleGenderAlert;
    private Alert unknownGenderAlert;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Alert {
      private String actionTitle;
      private String cancelTitle;
      private boolean enabled;
      private String message;
    }

  }

  @Getter
  @Setter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class FingerprintedDriverTypeConfiguration extends DefaultDriverTypeConfiguration {
    private String title;
    private String menuTitle;
    private String iconUrl;
    private String menuIconUrl;
  }

}
