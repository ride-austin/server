package com.rideaustin.events.listeners;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.ImmutableMap;
import com.rideaustin.events.TNCCardUpdateEvent;
import com.rideaustin.model.Document;
import com.rideaustin.model.enums.CityApprovalStatus;
import com.rideaustin.model.enums.DocumentStatus;
import com.rideaustin.model.user.Driver;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.service.CurrentUserService;

public class TNCCardUpdateEventListenerTest {

  @Mock
  private DocumentDslRepository documentDslRepository;
  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private Driver driver;
  @Mock
  private Document card;
  @Mock
  private CurrentUserService currentUserService;

  private TNCCardUpdateEventListener testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = new TNCCardUpdateEventListener(documentDslRepository, driverDslRepository, currentUserService);
  }

  @Test
  public void testHandleTNCCardUpdateEventResolvesNewDriverStatus() throws Exception {
    Map<DocumentStatus, CityApprovalStatus> mapping = ImmutableMap.of(
      DocumentStatus.APPROVED, CityApprovalStatus.APPROVED,
      DocumentStatus.EXPIRED, CityApprovalStatus.EXPIRED,
      DocumentStatus.PENDING, CityApprovalStatus.PENDING,
      DocumentStatus.REJECTED, CityApprovalStatus.REJECTED_BY_CITY
    );

    when(documentDslRepository.findDriver(eq(card))).thenReturn(driver);
    when(driverDslRepository.findById(any())).thenReturn(driver);

    for (Map.Entry<DocumentStatus, CityApprovalStatus> entry : mapping.entrySet()) {
      TNCCardUpdateEvent event = new TNCCardUpdateEvent(card, entry.getKey());
      testedInstance.handleTNCCardUpdateEvent(event);
      verify(driver).setCityApprovalStatus(eq(entry.getValue()));
    }
  }

}