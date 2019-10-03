package com.rideaustin.service;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.maps.model.LatLng;
import com.rideaustin.clients.configuration.ConfigurationItemCache;
import com.rideaustin.config.CacheConfiguration;
import com.rideaustin.filter.ClientType;
import com.rideaustin.model.Campaign;
import com.rideaustin.model.CampaignArea;
import com.rideaustin.model.CampaignRider;
import com.rideaustin.model.enums.CampaignCoverageType;
import com.rideaustin.model.enums.ConfigurationWeekday;
import com.rideaustin.model.rest.CampaignSubscriptionDto;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.CampaignDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideTrackerDslRepository;
import com.rideaustin.repo.dsl.RiderDslRepository;
import com.rideaustin.service.strategy.CampaignEligibilityStrategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class CampaignService {

  private final ConfigurationItemCache configurationItemCache;

  private final RideDslRepository rideDslRepository;
  private final CampaignDslRepository repository;
  private final RideTrackerDslRepository rideTrackerDslRepository;
  private final RiderDslRepository riderDslRepository;
  private final BeanFactory beanFactory;

  public Optional<Campaign> findEligibleCampaign(final Date date, LatLng startLocation, String carCategory, Rider rider) {
    return findEligibleCampaign(date, startLocation, null, carCategory, rider, BigDecimal.ZERO);
  }

  public Optional<Campaign> findEligibleCampaign(final Date date, LatLng startLocation, LatLng endLocation,
    String carCategory, Rider rider, BigDecimal distance) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTime(date);
    final List<Campaign> campaigns = repository.findAll();
    Optional<Campaign> result = Optional.empty();
    for (int i = 0, campaignsSize = campaigns.size(); i < campaignsSize && !result.isPresent(); i++) {
      Campaign campaign = campaigns.get(i);
      final ConfigurationWeekday requestWeekDay = ConfigurationWeekday.fromWeekday(calendar.get(Calendar.DAY_OF_WEEK));
      if (!campaign.supportsWeekday(requestWeekDay)
        || !campaign.supportsRequestTime(date)
        || !campaign.supportsCarType(carCategory)
        || (campaign.isUserBound() && !campaign.supportsRider(rider))
        || (campaign.getMaximumDistance() != null && campaign.getMaximumDistance().compareTo(safeZero(distance)) < 0)) {
        continue;
      }

      final CampaignEligibilityStrategy eligibilityStrategy = beanFactory.getBean(campaign.getEligibilityStrategy());
      if (eligibilityStrategy.isEligible(startLocation, endLocation, rider, campaign)) {
        result = Optional.of(campaign);
      }
    }
    return result;
  }

  public Optional<Campaign> findMatchingCampaignForRide(long id) {
    return findMatchingCampaignForRide(rideDslRepository.findOneWithRider(id));
  }

  public Optional<Campaign> findMatchingCampaignForRide(Ride ride) {
    final Optional<Campaign> eligibleCampaign = findEligibleCampaign(ride.getRequestedOn(), new LatLng(ride.getStartLocationLat(), ride.getStartLocationLong()),
      new LatLng(ride.getEndLocationLat(), ride.getEndLocationLong()), ride.getRequestedCarType().getCarCategory(),
      rideDslRepository.findRider(ride.getId()), ride.getDistanceTravelled());
    if (!eligibleCampaign.isPresent()) {
      log.info(String.format("[CAMPAIGN][Ride %d] Ride is not started/ended within eligible zone of currently enabled campaigns", ride.getId()));
      return eligibleCampaign;
    }
    Campaign candidate = eligibleCampaign.get();
    if (candidate.isValidateTrackers()) {
      final RideTrackAnalyzer rideTrackAnalyzer = new RideTrackAnalyzer(ride.getId(),
        Optional.ofNullable(candidate.getTrackersValidationThreshold())
          .map(BigDecimal::doubleValue)
          .orElse(configurationItemCache.getConfigAsDouble(ClientType.CONSOLE, "campaigns", "trackerThreshold")));
      rideTrackerDslRepository.findAllTrackerRecord(ride.getId())
        .stream()
        .filter(RideTracker::getValid)
        .forEach(rideTrackAnalyzer::addTracker);
      if (!rideTrackAnalyzer.analyzeFor(candidate)) {
        return Optional.empty();
      }
    }
    if (candidate.getCoverageType() == CampaignCoverageType.FULL &&
      candidate.getMaximumCappedAmount() != null &&
      ride.getTotalCharge().isGreaterThan(candidate.getMaximumCappedAmount())) {
        return Optional.empty();
    } else {
      log.info(String.format("[Ride #%d] Candidate campaign type %s, capped %s, ride total %s",
        ride.getId(), candidate.getCoverageType(), candidate.getCappedAmount(), ride.getTotalCharge()));
    }
    return Optional.of(candidate);
  }

  @Transactional
  public void addRide(Campaign campaign, Ride ride) {
    campaign = repository.findOne(campaign.getId());
    campaign.getRides().add(ride);
    repository.save(campaign);
  }

  public Optional<Campaign> findExistingCampaignForRide(long id) {
    return Optional.ofNullable(repository.findCampaignForRide(rideDslRepository.findOne(id)));
  }

  public Optional<Campaign> findExistingCampaignForRide(Ride ride) {
    return Optional.ofNullable(repository.findCampaignForRide(ride));
  }

  @Transactional
  @CacheEvict(cacheNames = CacheConfiguration.CAMPAIGNS_CACHE, allEntries = true)
  public void subscribeRider(long campaignId, long riderId) {
    final Rider rider = riderDslRepository.getRider(riderId);
    final Campaign campaign = repository.findOne(campaignId);
    if (campaign.supportsRider(rider, false)) {
      campaign.enableRider(rider);
    } else {
      CampaignRider subscription = new CampaignRider(rider, campaign);
      repository.saveAny(subscription);
    }
    repository.save(campaign);
  }

  @Transactional
  @CacheEvict(cacheNames = CacheConfiguration.CAMPAIGNS_CACHE, allEntries = true)
  public void unsubscribeRider(long campaignId, long riderId) {
    final Rider rider = riderDslRepository.getRider(riderId);
    final Campaign campaign = repository.findOne(campaignId);
    if (campaign.supportsRider(rider, false)) {
      campaign.disableRider(rider);
    }
    repository.save(campaign);
  }

  public List<CampaignSubscriptionDto> listSubscriptions(long riderId) {
    final Rider rider = riderDslRepository.getRider(riderId);
    return repository.findAll()
      .stream()
      .filter(c -> !c.getProvider().isShownInMenu() && c.isEnabled())
      .map(c -> new CampaignSubscriptionDto(c.getId(), c.getName(), c.supportsRider(rider)))
      .collect(Collectors.toList());
  }

  public static class RideTrackAnalyzer {
    private final long rideId;
    private final Double threshold;

    private final List<RideTracker> trackers = new LinkedList<>();

    RideTrackAnalyzer(long rideId, Double threshold) {
      this.rideId = rideId;
      this.threshold = threshold;
    }

    void addTracker(RideTracker tracker) {
      trackers.add(tracker);
    }

    boolean analyzeFor(Campaign campaign) {
      if (trackers.isEmpty()) {
        log.info(String.format("[CAMPAIGN][Ride %d] Ride trackers are absent", rideId));
        return false;
      }
      boolean startTrackerInPickupZone = false;
      for (CampaignArea area : campaign.getPickupZones()) {
        if (area.contains(trackers.get(0).getLatitude(), trackers.get(0).getLongitude())) {
          startTrackerInPickupZone = true;
          break;
        }
      }
      if (!startTrackerInPickupZone) {
        log.info(String.format("[CAMPAIGN][Ride %d] Ride is requested in the zone but started out of the zone", rideId));
        return false;
      }
      Set<CampaignArea> allAreas = new HashSet<>(campaign.getPickupZones());
      allAreas.addAll(campaign.getDropoffZones());
      int inZone = 0;
      for (RideTracker tracker : trackers) {
        if (allAreas.stream().anyMatch(a -> a.contains(tracker.getLatitude(), tracker.getLongitude()))) {
          inZone++;
        }
      }
      log.info(String.format("[CAMPAIGN][Ride %d] Found %d ride trackers in the zone, with limit value of %.2f", rideId,
        inZone, Math.floor((double) trackers.size() * threshold)));
      return inZone >= Math.floor((double) trackers.size() * threshold);
    }

  }

}