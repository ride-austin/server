package com.rideaustin.service.notification;

import static com.rideaustin.test.util.TestUtils.money;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.joda.money.Money;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.rideaustin.Constants;
import com.rideaustin.model.RidePushNotificationDTO;
import com.rideaustin.model.RidePushNotificationRepository;
import com.rideaustin.model.Token;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.jpa.TokenRepository;
import com.rideaustin.service.CityCache;
import com.rideaustin.service.notifications.PushNotificationsService;

public class RideFlowPushNotificationFacadeTest {

  @Mock
  private TokenRepository tokenRepository;
  @Mock
  private CityCache cityCache;
  @Mock
  private PushNotificationsService notificationsService;
  @Mock
  private Environment environment;
  @Mock
  private RidePushNotificationRepository repository;

  private RideFlowPushNotificationFacade testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new RideFlowPushNotificationFacade(tokenRepository, cityCache, notificationsService, environment, repository);
    when(tokenRepository.findByUserAndAvatarType(any(User.class), eq(AvatarType.RIDER))).thenReturn(Collections.singletonList(new Token()));
  }

  @Test
  public void sendUpdateToRiderNAD() {
    setupNotification("", RideStatus.NO_AVAILABLE_DRIVER, Constants.ZERO_USD);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("NO_AVAILABLE_DRIVER", "No available drivers"))
    );
  }

  @Test
  public void sendUpdateToRiderDriverAssigned() {
    final String driverName = "ABC";
    setupNotification(driverName, RideStatus.DRIVER_ASSIGNED, Constants.ZERO_USD);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("DRIVER_ASSIGNED", String.format("Your ride request is accepted by %s.", driverName)))
    );
  }

  @Test
  public void sendUpdateToRiderDriverCancellationWithoutCancellationFee() {
    final String driverName = "ABC";
    setupNotification(driverName, RideStatus.DRIVER_CANCELLED, Constants.ZERO_USD);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("DRIVER_CANCELLED", String.format("Your ride request is cancelled by %s.", driverName)))
    );
  }

  @Test
  public void sendUpdateToRiderDriverCancellationWithCancellationFee() {
    final String driverName = "ABC";
    final Money cancellationFee = money(10.0);
    setupNotification(driverName, RideStatus.DRIVER_CANCELLED, cancellationFee);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("DRIVER_CANCELLED", String.format("Your ride has been cancelled by %s. You've been charged a %s cancellation fee for no show.", driverName, cancellationFee.toString())))
    );
  }

  @Test
  public void sendUpdateToRiderAdminCancellation() {
    setupNotification("", RideStatus.ADMIN_CANCELLED, Constants.ZERO_USD);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("ADMIN_CANCELLED", "Your ride request has been cancelled by an administrator."))
    );
  }

  @Test
  public void sendUpdateToRiderDriverReached() {
    final String driverName = "ABC";
    setupNotification(driverName, RideStatus.DRIVER_REACHED, Constants.ZERO_USD);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("DRIVER_REACHED", String.format("%s has arrived at your location", driverName)))
    );
  }

  @Test
  public void sendUpdateToRiderRideStarted() {
    setupNotification("", RideStatus.ACTIVE, Constants.ZERO_USD);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("RIDE_ACTIVE", "Your ride is starting."))
    );
  }

  @Test
  public void sendUpdateToRiderRideCompleted() {
    setupNotification("", RideStatus.COMPLETED, Constants.ZERO_USD);

    testedInstance.sendRideUpdateToRider(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("RIDE_COMPLETED", "Your ride is completed."))
    );
  }

  @Test
  public void pushRideRedispatchNotificationSendsNotification() {
    setupNotification("", RideStatus.REQUESTED, Constants.ZERO_USD);

    testedInstance.pushRideRedispatchNotification(1L);

    verify(notificationsService).publishNotification(
      anyListOf(Token.class),
      argThat(new PushContentMatcher("RIDE_REDISPATCHED", "Ride redispatched to next driver"))
    );
  }

  private void setupNotification(final String driverName, final RideStatus status, final Money cancellationFee) {
    when(repository.get(anyLong())).thenReturn(new RidePushNotificationDTO(1L, driverName, status, cancellationFee, new User()));
  }

  private static class PushContentMatcher extends BaseMatcher<Map<String, String>> {

    private final String expectedEventKey;
    private final String expectedMessage;

    private PushContentMatcher(String expectedEventKey, String expectedMessage) {
      this.expectedEventKey = expectedEventKey;
      this.expectedMessage = expectedMessage;
    }

    @Override
    public boolean matches(Object o) {
      final Map<String, String> dataMap = (Map<String, String>) o;
      return dataMap.get("eventKey").equals(expectedEventKey)
        && dataMap.get("alert").equals(expectedMessage);
    }

    @Override
    public void describeTo(Description description) {

    }
  }
}