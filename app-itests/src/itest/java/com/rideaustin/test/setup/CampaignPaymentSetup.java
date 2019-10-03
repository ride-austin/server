package com.rideaustin.test.setup;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.ride.RideTracker;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideTrackerDslRepository;
import com.rideaustin.service.user.CarTypesCache;
import com.rideaustin.test.fixtures.RiderFixture;
import com.rideaustin.test.fixtures.providers.ActiveDriverFixtureProvider;

@Component
public class CampaignPaymentSetup implements SetupAction<CampaignPaymentSetup> {

  @Inject
  @Named("simpleRiderWithoutCharity")
  private RiderFixture riderFixture;

  @Inject
  private ActiveDriverFixtureProvider activeDriverFixtureProvider;

  @Inject
  private ObjectMapper objectMapper;
  @Inject
  private CarTypesCache carTypesCache;
  @Inject
  private RideTrackerDslRepository trackerDslRepository;

  @Inject
  private RideDslRepository rideDslRepository;
  private Ride ride;

  @Override
  @Transactional
  public CampaignPaymentSetup setUp() throws Exception {
    final MapType mapType = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
    final Map<String, Object> map = objectMapper.readValue(this.getClass().getResource("ride.json"), mapType);
    Ride mappedRide = new Ride();
    mappedRide.setRider(riderFixture.getFixture());
    mappedRide.setActiveDriver(activeDriverFixtureProvider.create().getFixture());
    mappedRide.setCreatedDate(parseDate(map.get("created_date")));
    mappedRide.setRequestedOn(parseDate(map.get("requested_on")));
    FareDetails fareDetails = new FareDetails();
    fareDetails.setAirportFee(Constants.ZERO_USD);
    fareDetails.setBaseFare(parseMoney(map.get("base_fare")));
    fareDetails.setBookingFee(parseMoney(map.get("booking_fee")));
    fareDetails.setCancellationFee(null);
    fareDetails.setCityFee(parseMoney(map.get("city_fee")));
    fareDetails.setDistanceFare(parseMoney(map.get("distance_fare")));
    fareDetails.setDriverPayment(parseMoney(map.get("driver_payment")));
    fareDetails.setFreeCreditCharged(parseMoney(map.get("free_credit_used")));
    fareDetails.setMinimumFare(parseMoney(map.get("minimum_fare")));
    fareDetails.setNormalFare(parseMoney(map.get("normal_fare")));
    fareDetails.setProcessingFee(parseMoney(map.get("processing_fee")));
    fareDetails.setRaPayment(parseMoney(map.get("ra_payment")));
    fareDetails.setRatePerMile(parseMoney(map.get("rate_per_mile")));
    fareDetails.setRatePerMinute(parseMoney(map.get("rate_per_minute")));
    fareDetails.setRoundUpAmount(parseMoney(map.get("round_up_amount")));
    fareDetails.setSubTotal(parseMoney(map.get("sub_total")));
    fareDetails.setSurgeFare(parseMoney(map.get("surge_fare")));
    fareDetails.setTimeFare(parseMoney(map.get("time_fare")));
    fareDetails.setTotalFare(parseMoney(map.get("total_fare")));
    mappedRide.setFareDetails(fareDetails);
    mappedRide.setCityId(1L);
    mappedRide.setCompletedOn(parseDate(map.get("completed_on")));
    mappedRide.setDistanceTravelled(parseBigDecimal(map.get("distance_travelled")));
    mappedRide.setEndLocationLat((Double) map.get("end_location_lat"));
    mappedRide.setEndLocationLong((Double) map.get("end_location_long"));
    mappedRide.setStartLocationLat((Double) map.get("start_location_lat"));
    mappedRide.setStartLocationLong((Double) map.get("start_location_long"));
    mappedRide.setStartedOn(parseDate(map.get("started_on")));
    mappedRide.setStatus(RideStatus.valueOf((String) map.get("status")));
    mappedRide.setDriverReachedOn(parseDate(map.get("driver_reached_on")));
    mappedRide.setDriverAcceptedOn(parseDate(map.get("driver_accepted_on")));
    mappedRide.setRequestedCarType(carTypesCache.getCarType((String) map.get("requested_car_category")));
    mappedRide.setSurgeFactor(parseBigDecimal(map.get("surge_factor")));
    ride = rideDslRepository.save(mappedRide);
    ride.setCreatedDate(parseDate(map.get("created_date")));
    ride = rideDslRepository.save(mappedRide);
    AtomicLong seq = new AtomicLong(System.currentTimeMillis()/1000);
    final List<RideTracker> trackers = ((List<Map<String, Object>>) map.get("trackers"))
      .stream()
      .map(m -> {
        final RideTracker rideTracker = new RideTracker();
        rideTracker.setValid((Boolean) m.get("valid"));
        rideTracker.setLatitude((Double) m.get("latitude"));
        rideTracker.setLongitude((Double) m.get("longitude"));
        rideTracker.setRideId(ride.getId());
        rideTracker.setSequence(seq.getAndIncrement());
        return rideTracker;
      })
      .collect(Collectors.toList());
    trackerDslRepository.saveAnyMany(trackers);
    return this;
  }

  protected BigDecimal parseBigDecimal(final Object value) {
    return BigDecimal.valueOf((Double) value);
  }

  protected Money parseMoney(final Object value) {
    return Money.of(CurrencyUnit.USD, ((Double) value));
  }

  protected Date parseDate(final Object value) {
    return Date.from(LocalDateTime.parse((CharSequence) value).toInstant(ZoneOffset.UTC));
  }

  public Ride getRide() {
    return ride;
  }
}
