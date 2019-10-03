package com.rideaustin.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import com.google.common.collect.Lists;
import com.querydsl.core.Tuple;
import com.rideaustin.Constants;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.RideReportDslRepository;
import com.rideaustin.repo.jpa.DriverRidesReportRepository;
import com.rideaustin.rest.model.ListRidesParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.model.CumulativeRidesReportEntry;
import com.rideaustin.service.model.DriverRidesReportEntry;
import com.rideaustin.service.model.RideReportEntry;
import com.rideaustin.service.model.ZipCodeReportEntry;
import com.rideaustin.utils.DateUtils;

public class RideReportServiceTest {

  @Mock
  private RideReportDslRepository rideReportDslRepository;
  @Mock
  private DriverRidesReportRepository driverRidesReportRepository;
  @Mock
  private RideDslRepository rideDslRepository;

  private RideReportService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    List<Ride> rides = new ArrayList<>();
    List<RideReportEntry> rideReportEntries = new ArrayList<>();
    List<DriverRidesReportEntry> driverRidesReportEntries = new ArrayList<>();
    List<ZipCodeReportEntry> zipCodeReportEntries = new ArrayList<>();
    CumulativeRidesReportEntry cumulativeRidesReportEntry = new CumulativeRidesReportEntry();


    rideReportEntries.add(createSampleRideReportEntry());
    rideReportEntries.add(createSampleRideReportEntry());

    driverRidesReportEntries.add(createSampleDriverRidesReportEntry());
    driverRidesReportEntries.add(createSampleDriverRidesReportEntry());

    zipCodeReportEntries.add(createSampleZipCodeReportEntry());
    zipCodeReportEntries.add(createSampleZipCodeReportEntry());

    cumulativeRidesReportEntry.setDriversRidesReport(new PageImpl<>(driverRidesReportEntries,
      new PagingParams().toPageRequest(), 2L));
    cumulativeRidesReportEntry.setRidesReport(rideReportEntries);

    rides.add(createSampleRide());
    rides.add(createSampleRide());

    when(driverRidesReportRepository.driverRideReport(any(), any(), any(), any(), any()))
      .thenReturn(new PageImpl<>(driverRidesReportEntries, new PagingParams().toPageRequest(), 2L));
    when(rideReportDslRepository.getZipCodeReport(any(), any(), any(), any())).thenReturn(new PageImpl(zipCodeReportEntries, new PagingParams().toPageRequest(), 2L));
    when(rideDslRepository.ridesList(any())).thenReturn(rides);

    testedInstance = new RideReportService(rideReportDslRepository, driverRidesReportRepository, rideDslRepository);
  }

  @Test
  public void testGetRidesReportIncludesCancelledTripsWithCancellationFee() {
    Ride completedRide = new Ride();
    completedRide.setStatus(RideStatus.COMPLETED);
    completedRide.setCompletedOn(DateUtils.localDateToDate(LocalDate.of(2016, 11, 10)));
    completedRide.setDistanceTravelled(BigDecimal.ONE);
    FareDetails fareDetails = FareDetails.builder().totalFare(Money.of(CurrencyUnit.USD, BigDecimal.ONE)).build();
    completedRide.setFareDetails(fareDetails);

    Ride chargedCancelledRide = new Ride();
    chargedCancelledRide.setStatus(RideStatus.RIDER_CANCELLED);
    chargedCancelledRide.setCancelledOn(DateUtils.localDateToDate(LocalDate.of(2016, 11, 10)));
    chargedCancelledRide.setDistanceTravelled(BigDecimal.ZERO);
    FareDetails chargedCancelledFareDetails = FareDetails.builder()
      .totalFare(Money.of(CurrencyUnit.USD, BigDecimal.ONE))
      .cancellationFee(Money.of(CurrencyUnit.USD, BigDecimal.ONE))
      .build();
    chargedCancelledRide.setFareDetails(chargedCancelledFareDetails);
    Ride notChargedCancelledRide = new Ride();
    notChargedCancelledRide.setStatus(RideStatus.DRIVER_CANCELLED);
    notChargedCancelledRide.setCancelledOn(DateUtils.localDateToDate(LocalDate.of(2016, 11, 10)));
    notChargedCancelledRide.setDistanceTravelled(BigDecimal.ZERO);
    FareDetails notChargedCancelledFareDetails = FareDetails.builder()
      .totalFare(Money.of(CurrencyUnit.USD, BigDecimal.ZERO))
      .cancellationFee(Money.of(CurrencyUnit.USD, BigDecimal.ZERO))
      .build();
    notChargedCancelledRide.setFareDetails(notChargedCancelledFareDetails);
    when(rideDslRepository.ridesList(any(ListRidesParams.class))).thenReturn(Lists.newArrayList(completedRide, chargedCancelledRide, notChargedCancelledRide));

    List<RideReportEntry> report = testedInstance.getRidesReport(Instant.now(), Instant.now(), null, Constants.DEFAULT_CITY_ID, "-06:00");

    assertEquals(1, report.size());
    RideReportEntry entry = report.get(0);
    assertEquals(1L, entry.getRidesCount().longValue());
    assertEquals(2L, entry.getCancelledRidesCount().longValue());
    assertEquals(2L, entry.getTotalFares().longValue());
  }

  @Test
  public void getRidesReportTestWithZipCodeTest() {
    List<RideReportEntry> ret = testedInstance.getRidesReport(Instant.now(), Instant.now(), "asd", Constants.DEFAULT_CITY_ID, null);
    assertEquals(ret.size(), 1);
  }

  @Test
  public void getRidesByUsersReportTest() {
    Page<DriverRidesReportEntry> ret = testedInstance.getRidesByUsersReport(Instant.now(), Instant.now(), "asd", Constants.DEFAULT_CITY_ID, new PagingParams());
    assertEquals(ret.getContent().size(), 2);
  }

  @Test
  public void getRidesZipCodeReportTest() {
    Page<Tuple> ret = testedInstance.getRidesZipCodeReport(Instant.now(), Instant.now(), null, null);
    assertEquals(ret.getTotalElements(), 2L);
  }

  @Test
  public void getRidesReportTestNoZipCodeTest() {
    List<RideReportEntry> ret = testedInstance.getRidesReport(Instant.now(), Instant.now(), null, Constants.DEFAULT_CITY_ID, null);
    assertEquals(ret.size(), 1);
  }

  @Test
  public void getRidesByUsersReportNoZipCodeTest() {
    Page<DriverRidesReportEntry> ret = testedInstance.getRidesByUsersReport(Instant.now(), Instant.now(), null, Constants.DEFAULT_CITY_ID, null);
    assertEquals(ret.getContent().size(), 2);
  }

  @Test
  public void getCumulativeRidesReportWithZipCodeTest() {
    CumulativeRidesReportEntry ret = testedInstance.getCumulativeRidesReport(Instant.now(), Instant.now(), "RWA", Constants.DEFAULT_CITY_ID, null);
    assertEquals(ret.getDriversRidesReport().getContent().size(), (2));
    assertEquals(ret.getRidesReport().size(), (1));
  }

  @Test
  public void getCumulativeRidesReportTest() {
    CumulativeRidesReportEntry ret = testedInstance.getCumulativeRidesReport(Instant.now(), Instant.now(), null, Constants.DEFAULT_CITY_ID, null);
    assertEquals(ret.getDriversRidesReport().getContent().size(), (2));
    assertEquals(ret.getRidesReport().size(), (1));
  }

  private RideReportEntry createSampleRideReportEntry() {
    Random r = new Random();
    return new RideReportEntry(Date.from(LocalDate.of(2016, 10, 10).atStartOfDay().toInstant(ZoneOffset.UTC)),
      r.nextLong(), new BigDecimal(r.nextInt()),
      new BigDecimal(r.nextInt()), new BigDecimal(r.nextInt()), r.nextDouble(), r.nextLong());
  }

  private DriverRidesReportEntry createSampleDriverRidesReportEntry() {
    Random r = new Random();
    return new DriverRidesReportEntry(r.nextLong(), r.nextLong(), "John", "Smith",
      r.nextLong(), r.nextLong(), new BigDecimal(r.nextInt()), new BigDecimal(r.nextInt()), new BigDecimal(r.nextInt()),
      new BigDecimal(r.nextInt()), new BigDecimal(r.nextInt()), new BigDecimal(r.nextInt()), new BigDecimal(r.nextInt()),
      new BigDecimal(r.nextInt()));
  }

  private ZipCodeReportEntry createSampleZipCodeReportEntry() {
    Random r = new Random();
    return new ZipCodeReportEntry("zip-code", r.nextLong());
  }

  private Ride createSampleRide() {
    Ride r = new Ride();
    r.setDistanceTravelled(BigDecimal.TEN);
    r.setCompletedOn(new Date());
    r.setFareDetails(FareDetails.builder().totalFare(Money.of(CurrencyUnit.USD, 12d)).build());
    r.setStatus(RideStatus.COMPLETED);
    return r;
  }
}