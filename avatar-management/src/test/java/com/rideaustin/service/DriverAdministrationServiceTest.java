package com.rideaustin.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageImpl;

import com.rideaustin.model.user.Driver;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.AvatarDocumentDslRepository;
import com.rideaustin.repo.dsl.DocumentDslRepository;
import com.rideaustin.repo.dsl.DriverDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.RideAustinException;
import com.rideaustin.rest.model.ConsoleDriverDto;
import com.rideaustin.rest.model.ListDriversParams;
import com.rideaustin.rest.model.PagingParams;
import com.rideaustin.service.event.EventsNotificationService;
import com.rideaustin.service.ride.DirectConnectService;
import com.rideaustin.service.thirdparty.PayoneerService;
import com.rideaustin.service.user.DriverTypeCache;
import com.rideaustin.service.user.DriverTypeUtils;

public class DriverAdministrationServiceTest {

  @Mock
  private DriverDslRepository driverDslRepository;
  @Mock
  private DocumentDslRepository documentDslRepository;

  @Mock
  private SessionService sessionService;
  @Mock
  private DriverTypeCache driverTypeCache;
  @Mock
  private BaseAvatarService baseAvatarService;
  @Mock
  private PayoneerService payoneerService;
  @Mock
  private CurrentUserService currentUserService;
  @Mock
  private DriverService driverService;
  @Mock
  private DirectConnectService directConnectService;
  @Mock
  private UserService userService;
  @Mock
  private EventsNotificationService notificationService;
  @Mock
  private CurrentSessionService currentSessionService;
  @Mock
  private AvatarDocumentDslRepository avatarDocumentDslRepository;
  @Mock
  private DriverEmailReminderService driverEmailReminderService;
  @Mock
  private ActiveDriversService activeDriversService;
  @Mock
  private RideDslRepository rideDslRepository;

  @Mock
  private ApplicationEventPublisher publisher;

  private DriverAdministrationService driverAdministrationService;

  private Driver driver = new Driver();
  private User user = new User();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    driverAdministrationService = new DriverAdministrationService(driverDslRepository, avatarDocumentDslRepository,
      documentDslRepository, userService, notificationService, directConnectService, driverEmailReminderService,
      sessionService, currentSessionService, driverTypeCache, baseAvatarService, payoneerService, currentUserService,
      driverService, activeDriversService, rideDslRepository, publisher);
    driver.setUser(user);
  }

  @Test
  public void testListRidersEnrichLastLoginDate() {
    PageImpl<Driver> driversPage = new PageImpl<>(Collections.singletonList(driver));
    when(driverDslRepository.findDrivers(any(), any())).thenReturn(driversPage);

    driverAdministrationService.listDrivers(new ListDriversParams(), new PagingParams());

    verify(baseAvatarService, times(1)).enrichAvatarWithLastLoginDate(driversPage);
  }

  @Test
  public void testUpdateDriverUpdatesSsnUpdateOnNonNullSsnProvided() throws RideAustinException {
    DriverTypeUtils.setDriverTypeCache(mock(DriverTypeCache.class));
    Driver current = mock(Driver.class);
    when(current.getUser()).thenReturn(new User());
    ConsoleDriverDto driver = mock(ConsoleDriverDto.class);
    final ConsoleDriverDto.UserDto user = mock(ConsoleDriverDto.UserDto.class);
    when(driver.getUser()).thenReturn(user);
    when(driverService.findDriver(anyLong(), any())).thenReturn(current);
    when(driver.getSsn()).thenReturn("123456789");

    driverAdministrationService.updateDriver(1L, driver);

    verify(current, times(1)).setSsn(endsWith("6789"));
  }

  @Test
  public void testUpdateDriverUpdatesDCIDWhenDCIDIsNotTaken() throws RideAustinException {
    DriverTypeUtils.setDriverTypeCache(mock(DriverTypeCache.class));
    Driver current = mock(Driver.class);
    when(current.getUser()).thenReturn(new User());
    ConsoleDriverDto driver = mock(ConsoleDriverDto.class);
    final ConsoleDriverDto.UserDto user = mock(ConsoleDriverDto.UserDto.class);
    when(driver.getUser()).thenReturn(user);
    when(driver.getDirectConnectId()).thenReturn("11111");
    when(driverService.findDriver(anyLong(), any())).thenReturn(current);

    driverAdministrationService.updateDriver(1L, driver);

    verify(current, times(1)).setDirectConnectId("11111");
  }

  @Test(expected = BadRequestException.class)
  public void testUpdateDriverThrowsExceptionWhenDCIDIsTaken() throws RideAustinException {
    DriverTypeUtils.setDriverTypeCache(mock(DriverTypeCache.class));
    Driver current = mock(Driver.class);
    when(current.getUser()).thenReturn(new User());
    ConsoleDriverDto driver = mock(ConsoleDriverDto.class);
    final ConsoleDriverDto.UserDto user = mock(ConsoleDriverDto.UserDto.class);
    when(driver.getUser()).thenReturn(user);
    when(driver.getDirectConnectId()).thenReturn("11111");
    when(driverService.findDriver(anyLong(), any())).thenReturn(current);
    doThrow(new BadRequestException(""))
      .when(directConnectService).validateDirectConnectId("11111");

    driverAdministrationService.updateDriver(1L, driver);

  }

}