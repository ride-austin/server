package com.rideaustin.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.Where;
import org.joda.money.Money;

import com.rideaustin.Constants;
import com.rideaustin.model.enums.CampaignCoverageType;
import com.rideaustin.model.enums.ConfigurationWeekday;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.service.strategy.CampaignEligibilityStrategy;
import com.rideaustin.service.user.CarTypesUtils;
import com.rideaustin.utils.DateUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

  @Column(name = "name")
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "description_trip_history")
  private String tripHistoryDescription;

  @Column(name = "enabled")
  private boolean enabled;

  @Column(name = "eligible_car_categories")
  private int eligibleCarCategories;

  @Column(name = "capped_amount")
  private Money cappedAmount;

  @Column(name = "maximum_capped_amount")
  private Money maximumCappedAmount;

  @Column(name = "tipping_allowed")
  private boolean tippingAllowed;

  @Column(name = "coverage_type")
  @Enumerated(value = EnumType.STRING)
  private CampaignCoverageType coverageType;

  @Column(name = "accessibility_config")
  private String accessibilityConfig;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "campaign_id")
  @Where(clause = "type = 'PICKUP'")
  private Set<CampaignArea> pickupZones;

  @OneToMany(fetch = FetchType.EAGER)
  @JoinColumn(name = "campaign_id")
  @Where(clause = "type = 'DROPOFF'")
  private Set<CampaignArea> dropoffZones;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "campaign_rides", joinColumns = @JoinColumn(name = "campaign_id"), inverseJoinColumns = @JoinColumn(name = "ride_id"))
  private Set<Ride> rides;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "campaign", cascade = CascadeType.ALL)
  private Set<CampaignRider> subscribedRiders;

  @Column(name = "eligibility_strategy")
  private Class<CampaignEligibilityStrategy> eligibilityStrategy;

  @Column(name = "active_on_days")
  private int activeOnDays;

  @Column(name = "active_from_hour")
  private int activeFromHour;

  @Column(name = "active_to_hour")
  private int activeToHour;

  @Column(name = "banner_icon")
  private String bannerIcon;

  @Column(name = "receipt_image")
  private String receiptImage;

  @Column(name = "receipt_title")
  private String receiptTitle;

  @Column(name = "header_icon")
  private String headerIcon;

  @Column(name = "description_body")
  @Type(type = "text")
  private String descriptionBody;

  @Column(name = "footer_text")
  private String footerText;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "provider_id")
  private CampaignProvider provider;

  @Column(name = "show_map")
  private boolean showMap;

  @Column(name = "show_details")
  private boolean showDetails;

  @Column(name = "maximum_distance")
  private BigDecimal maximumDistance;

  @Column(name = "is_user_bound")
  private boolean userBound;

  @Column(name = "validate_trackers")
  private boolean validateTrackers;

  @Column(name = "trackers_validation_threshold")
  private BigDecimal trackersValidationThreshold;

  @Column(name = "report_recipients")
  private String reportRecipients;

  public boolean supportsRequestTime(Date date) {
    return DateUtils.isWithinHours(date, activeFromHour, activeToHour);
  }

  public boolean supportsWeekday(ConfigurationWeekday weekday) {
    return ConfigurationWeekday.fromBitmask(activeOnDays).contains(weekday);
  }

  public boolean supportsCarType(String carCategory) {
    return CarTypesUtils.fromBitMask(eligibleCarCategories).contains(carCategory);
  }

  public boolean supportsRider(Rider rider) {
    return supportsRider(rider, true);
  }

  public boolean supportsRider(Rider rider, boolean filterEnabled) {
    Stream<CampaignRider> riderStream = subscribedRiders
      .stream();
    if (filterEnabled) {
      riderStream = riderStream
        .filter(CampaignRider::isEnabled);
    }
    return riderStream
      .anyMatch(r -> r.getRider().equals(rider));
  }

  public void enableRider(Rider rider) {
    findRider(rider)
      .ifPresent(r -> r.setEnabled(true));
  }

  public void disableRider(Rider rider) {
    findRider(rider)
      .ifPresent(r -> r.setEnabled(false));
  }

  private Optional<CampaignRider> findRider(Rider rider) {
    return subscribedRiders.stream()
      .filter(r -> r.getRider().equals(rider))
      .findFirst();
  }

  public Money adjustTotalCharge(Money totalCharge) {
    return CampaignCoverageType.FULL.equals(getCoverageType()) ?
      Constants.ZERO_USD : totalCharge.minus(getCappedAmount());
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
}
