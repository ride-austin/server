package com.rideaustin.service;

import static com.google.common.collect.Lists.newArrayList;
import static com.rideaustin.service.DriverAuditedService.SYSTEM_USERNAME;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.ChangeDto;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.DriverAudited;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.DriverAuditedDslRepository;

@RunWith(MockitoJUnitRunner.class)
public class DriverAuditedServiceTest {

  private static final long ID = 123L;
  private static final String OTHER_USERNAME = "other username";
  private static final String FIRSTNAME = "a";
  private static final String LASTNAME = "b";
  private static final DriverAudited PREVIOUS_DRIVER =
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16);
  private static final List<DriverAudited> DRIVERS_AUDITED = Arrays.asList(
    getDA(1, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 1, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 1, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 1, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 0, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 0, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 0L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "0", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "0", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "0", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 40, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "40", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "40", 15d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 40d, 16),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 40),
    getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16)
  );
  private static final Date AUDIT_DAY = new Date();
  @Mock
  private DriverAuditedDslRepository driverAuditedDslRepository;
  @Mock
  private CurrentUserService currentUserService;
  @InjectMocks
  private DriverAuditedService driverAuditedService;

  private static Date date(int daysAgo) {
    return new DateTime(1489646485).minusDays(daysAgo).toDate();
  }

  private static DriverAudited getDA(int as, int cas, int os, int ps, int p1, int p3, long p4, String p5,
    String p8, String p9, int p10, String p11, String p13, double p14, int p15) {
    return DriverAudited.builder()
      .activationStatus(DriverActivationStatus.values()[as])
      .cityApprovalStatus(CityApprovalStatus.values()[cas])
      .onboardingStatus(DriverOnboardingStatus.values()[os])
      .payoneerStatus(PayoneerStatus.values()[ps])
      .grantedDriverTypesBitmask(p1)
      .activationDate(date(p3))
      .cityId(p4)
      .activationNotes(p5)
      .licenseNumber(p8)
      .licenseState(p9)
      .onboardingPendingSince(date(p10))
      .payoneerId(p11)
      .ssn(p13)
      .rating(p14)
      .agreementDate(date(p15))
      .build();
  }

  @Test
  public void shouldSaveForTheFirstTime() {
    // given
    DriverAudited driverAudited = DriverAudited.builder().build();
    when(driverAuditedDslRepository.getLastDriverAudited(ID)).thenReturn(null);

    // when
    driverAuditedService.saveIfChanged(driverAudited, null);

    // then
    assertEquals(SYSTEM_USERNAME, driverAudited.getUsername());
    verify(driverAuditedDslRepository, times(1)).saveAny(driverAudited);
  }

  @Test
  public void shouldNotSaveWhenIrrelevantFieldsChange() {
    // given
    DriverAudited driverAudited = withIrrelevantFields1();
    when(driverAuditedDslRepository.getLastDriverAudited(ID)).thenReturn(withIrrelevantFields2());
    when(currentUserService.getUser()).thenReturn(User.builder().email("email").build());

    // when
    driverAuditedService.saveIfChanged(driverAudited, null);

    // then
    assertEquals(OTHER_USERNAME, driverAudited.getUsername());
    verify(driverAuditedDslRepository, times(0)).saveAny(driverAudited);
  }

  @Test
  public void shouldSaveWhenRelevantFieldsChange() {
    for (DriverAudited driverAudited : DRIVERS_AUDITED) {
      testSaveOnRelevantFields(PREVIOUS_DRIVER, driverAudited);
    }
  }

  @Test
  public void shouldReturnNoChangesOnZeroAudits() {
    testReturnOneChange(0, newArrayList());
  }

  @Test
  public void shouldReturnNoChangesOnOneAudit() {
    testReturnOneChange(0, Collections.singletonList(new DriverAudited()));
  }

  @Test
  public void shouldReturnNoChangesOnNoChanges() {
    final DriverAudited driverAudited1 = new DriverAudited();
    final DriverAudited driverAudited2 = new DriverAudited();
    testReturnOneChange(0, Arrays.asList(driverAudited1, driverAudited2));
  }

  @Test
  public void shouldReturnNoChangesOnNoRelevantChanges() {
    testReturnOneChange(0, Arrays.asList(withIrrelevantFields1(), withIrrelevantFields2()));
  }

  @Test
  public void shouldReturnMaxChangesOnAllRelevantFieldsChange() {
    testReturnOneChange(
      15, Arrays.asList(
        getDA(0, 0, 0, 0, 1, 3, 4L, "5", "8", "9", 10, "11", "13", 15d, 16),
        getDA(1, 1, 1, 1, 0, 0, 0L, "0", "0", "0", 40, "40", "40", 40d, 40)));
  }

  @Test
  public void shouldReturnMaxChangesOnAllRelevantFieldsChangedAlmostTwice() {
    testReturnOneChange(29, DRIVERS_AUDITED);
  }

  @Test
  public void testReturnProperChange() {
    // given
    Driver driver = getDriver();
    String ssn1 = "ssn1";
    String ssn2 = "ssn2";
    Date revisionDate = new Date();
    long revision = 456L;
    when(driverAuditedDslRepository.findByDayAndDriverId(eq(ID), eq(AUDIT_DAY))).thenReturn(Arrays.asList(
      DriverAudited.builder().ssn(ssn1).build(),
      DriverAudited.builder().ssn(ssn2).revisionDate(revisionDate).username(OTHER_USERNAME).revision(revision).build()));

    // when
    List<ChangeDto> changes = driverAuditedService.getDriverChanges(driver, AUDIT_DAY);

    // then
    assertEquals(1, changes.size());

    ChangeDto change = changes.get(0);
    assertEquals(OTHER_USERNAME, change.getChangedBy());
    assertEquals("ssn", change.getChangedFieldName());
    assertEquals(FIRSTNAME + ' ' + LASTNAME, change.getEntityName());
    assertEquals(ssn2, change.getNewValue());
    assertEquals(ssn1, change.getPreviousValue());
    assertEquals(ID, change.getEntityId());
    assertEquals(revisionDate, change.getRevisionDate());
    assertEquals(revision, change.getRevision());
  }

  private void testSaveOnRelevantFields(DriverAudited previousDriver, DriverAudited driverAudited) {
    // given
    when(driverAuditedDslRepository.getLastDriverAudited(ID)).thenReturn(previousDriver);
    when(currentUserService.getUser()).thenReturn(User.builder().email(OTHER_USERNAME).build());

    // when
    driverAuditedService.saveIfChanged(driverAudited, currentUserService.getUser());

    // then
    assertEquals(OTHER_USERNAME, driverAudited.getUsername());
    verify(driverAuditedDslRepository, times(1)).saveAny(driverAudited);
  }

  private void testReturnOneChange(int expectedChangesCount, List<DriverAudited> auditedList) {
    // given
    Driver driver = getDriver();
    when(driverAuditedDslRepository.findByDayAndDriverId(eq(ID), eq(AUDIT_DAY))).thenReturn(auditedList);

    // when
    List<ChangeDto> changes = driverAuditedService.getDriverChanges(driver, AUDIT_DAY);

    // then
    assertEquals(expectedChangesCount, changes.size());
  }

  private Driver getDriver() {
    Driver driver = Driver.builder().build();
    driver.setId(ID);
    driver.setUser(User.builder().firstname(FIRSTNAME).lastname(LASTNAME).build());
    return driver;
  }

  private DriverAudited withIrrelevantFields2() {
    return DriverAudited.builder().id(ID).revision(2).revisionDate(new Date()).username(SYSTEM_USERNAME).build();
  }

  private DriverAudited withIrrelevantFields1() {
    return DriverAudited.builder().id(ID).revision(1).revisionDate(date(5)).username(OTHER_USERNAME).build();
  }
}