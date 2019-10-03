package com.rideaustin.repo.dsl;

import static io.codearte.catchexception.shade.mockito.Mockito.mock;
import static io.codearte.catchexception.shade.mockito.Mockito.when;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import javax.persistence.EntityManager;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DriverActivationStatus;
import com.rideaustin.model.enums.DriverOnboardingStatus;
import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.DriverAudited;
import com.rideaustin.service.DriverAuditedService;

@RunWith(MockitoJUnitRunner.class)
public class DriverDslRepositoryTest {

  @Mock
  private DriverAuditedService driverAuditedService;
  @Mock
  private EntityManager entityManager;
  @InjectMocks
  private DriverDslRepository driverDslRepository;

  @Before
  public void init() {
    entityManager = mock(EntityManager.class);
    driverDslRepository.entityManager = entityManager;
  }

  @Test
  public void shouldAuditDriverSave() {
    // given
    Driver driver = Driver.builder()
      .activationStatus(DriverActivationStatus.DEACTIVATED_OTHER)
      .cityApprovalStatus(CityApprovalStatus.REJECTED_BY_CITY)
      .onboardingStatus(DriverOnboardingStatus.PENDING)
      .payoneerStatus(PayoneerStatus.INITIAL)
      .grantedDriverTypesBitmask(1)
      .activationDate(date(3))
      .cityId(4L)
      .activationNotes("5")
      .licenseNumber("8")
      .licenseState("9")
      .onboardingPendingSince(date(10))
      .payoneerId("11")
      .ssn("13")
      .rating(14d)
      .agreementDate(date(15))
      .build();
    driver.setId(123L);
    when(entityManager.merge(driver)).thenReturn(driver);

    // when
    driverDslRepository.saveAs(driver, null);

    // then
    ArgumentCaptor<DriverAudited> captor = ArgumentCaptor.forClass(DriverAudited.class);
    verify(driverAuditedService, times(1)).saveIfChanged(captor.capture(), any());
    DriverAudited driverAudited = captor.getValue();
    assertEquals(driver.getId(), driverAudited.getId());
    assertEquals(driver.getAgreementDate(), driverAudited.getAgreementDate());
    assertEquals(driver.getSsn(), driverAudited.getSsn());
    assertEquals(driver.getLicenseNumber(), driverAudited.getLicenseNumber());
    assertEquals(driver.getLicenseState(), driverAudited.getLicenseState());
    assertEquals(driver.getRating(), driverAudited.getRating());
    assertEquals(driver.getPayoneerId(), driverAudited.getPayoneerId());
    assertEquals(driver.getPayoneerStatus(), driverAudited.getPayoneerStatus());
    assertEquals(driver.getActivationDate(), driverAudited.getActivationDate());
    assertEquals(driver.getCityApprovalStatus(), driverAudited.getCityApprovalStatus());
    assertEquals(driver.getActivationStatus(), driverAudited.getActivationStatus());
    assertEquals(driver.getActivationNotes(), driverAudited.getActivationNotes());
    assertEquals(driver.getOnboardingStatus(), driverAudited.getOnboardingStatus());
    assertEquals(driver.getOnboardingPendingSince(), driverAudited.getOnboardingPendingSince());
    assertEquals(driver.getGrantedDriverTypesBitmask(), driverAudited.getGrantedDriverTypesBitmask());
    assertEquals(driver.getCityId(), driverAudited.getCityId());
  }

  @Test
  public void shouldMaskSsnOnAuditDriverSave() {
    // given
    Driver driver = Driver.builder().ssn("44441234").build();
    driver.setId(123L);
    when(entityManager.merge(driver)).thenReturn(driver);

    // when
    driverDslRepository.saveAs(driver, null);

    // then
    ArgumentCaptor<DriverAudited> captor = ArgumentCaptor.forClass(DriverAudited.class);
    verify(driverAuditedService, times(1)).saveIfChanged(captor.capture(), any());
    DriverAudited driverAudited = captor.getValue();
    assertEquals(driver.getId(), driverAudited.getId());
    assertEquals("XXXX1234", driverAudited.getSsn());
  }

  private Date date(int daysAgo) {
    return new DateTime(1489646485).minusDays(daysAgo).toDate();
  }
}