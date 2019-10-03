package com.rideaustin.service.user;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anySetOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import com.rideaustin.events.AdminCancelledEvent;
import com.rideaustin.model.BlockedDevice;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.model.user.User;
import com.rideaustin.repo.dsl.BlockedDeviceDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.service.user.BlockedDeviceRegistry.BlockedDeviceMessage;
import com.rideaustin.service.user.BlockedDeviceRegistry.BlockedDeviceMessage.Type;

public class BlockedDeviceServiceTest {

  @Mock
  private BlockedDeviceDslRepository repository;
  @Mock
  private SessionDslRepository sessionDslRepository;
  @Mock
  private RideDslRepository rideDslRepository;
  @Mock
  private ApplicationEventPublisher publisher;
  @Mock
  private RedisTemplate redisTemplate;
  @Mock
  private ChannelTopic blockedDevicesTopic;

  private BlockedDeviceService testedInstance;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    testedInstance = new BlockedDeviceService(repository, sessionDslRepository, rideDslRepository, publisher, redisTemplate, blockedDevicesTopic);
  }

  @Test
  public void addToBlackListSavesNewEntries() {
    final String deviceId = "ABC";
    when(sessionDslRepository.findAllDeviceIds(any(User.class))).thenReturn(Collections.singletonList(deviceId));

    testedInstance.addToBlackList(new Rider());

    verify(repository).saveMany(argThat(new BaseMatcher<List<BlockedDevice>>() {
      @Override
      public boolean matches(Object o) {
        final List<BlockedDevice> list = (List<BlockedDevice>) o;
        return list.get(0).getDeviceId().equals(deviceId);
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  @Test
  public void addToBlackListSendsRedisMessage() {
    final String deviceId = "ABC";
    final String topic = "Topic";
    when(sessionDslRepository.findAllDeviceIds(any(User.class))).thenReturn(Collections.singletonList(deviceId));
    when(blockedDevicesTopic.getTopic()).thenReturn(topic);

    testedInstance.addToBlackList(new Rider());

    verify(redisTemplate).convertAndSend(eq(topic), argThat(new BlockedDeviceMessageMatcher(deviceId, Type.BLOCK)));
  }

  @Test
  public void addToBlackListPublishesEvent() {
    final String deviceId = "ABC";
    final long rideId = 1L;
    final Ride ride = new Ride();
    ride.setId(rideId);
    when(sessionDslRepository.findAllDeviceIds(any(User.class))).thenReturn(Collections.singletonList(deviceId));
    when(rideDslRepository.findByRiderAndStatus(any(Rider.class), anySetOf(RideStatus.class))).thenReturn(Collections.singletonList(ride));

    testedInstance.addToBlackList(new Rider());

    verify(publisher).publishEvent(argThat(new BaseMatcher<AdminCancelledEvent>() {
      @Override
      public boolean matches(Object o) {
        final AdminCancelledEvent event = (AdminCancelledEvent) o;
        return event.getRideId() == rideId;
      }

      @Override
      public void describeTo(Description description) {

      }
    }));
  }

  @Test
  public void unblockDelegatesCallToRepository() {
    final Rider rider = new Rider();

    testedInstance.unblock(rider);

    verify(repository).unblock(rider);
  }

  @Test
  public void unblockBroadcastsRedisMessage() {
    final Rider rider = new Rider();
    final String topic = "Topic";
    final String deviceId = "ABC";
    when(blockedDevicesTopic.getTopic()).thenReturn(topic);
    when(repository.unblock(any(Rider.class))).thenReturn(Collections.singletonList(deviceId));

    testedInstance.unblock(rider);

    verify(redisTemplate).convertAndSend(eq(topic), argThat(new BlockedDeviceMessageMatcher(deviceId, Type.UNBLOCK)));
  }

  @Test
  public void isInBlocklistDelegatesCallToRepository() {
    final long userId = 1L;

    testedInstance.isInBlocklist(userId);

    verify(repository).isBlocked(eq(userId));
  }

  @Test
  public void isInBlocklistRequiresNonNullDeviceId() {
    final boolean result = testedInstance.isInBlocklist(null);

    assertFalse(result);
  }

  @Test
  public void inInBlocklistReturnsRepositoryResult() {
    final String deviceId = "123";
    when(repository.isBlocked(eq(deviceId))).thenReturn(true);

    final boolean result = testedInstance.isInBlocklist(deviceId);

    assertTrue(result);
  }

  private static class BlockedDeviceMessageMatcher extends BaseMatcher<BlockedDeviceMessage> {

    private final String deviceId;
    private final Type messageType;

    BlockedDeviceMessageMatcher(String deviceId, final Type type) {
      this.deviceId = deviceId;
      this.messageType = type;
    }

    @Override
    public boolean matches(Object o) {
      final BlockedDeviceMessage message = (BlockedDeviceMessage) o;
      return message.getDeviceIds().contains(deviceId) && messageType.equals(message.getType());
    }

    @Override
    public void describeTo(Description description) {

    }
  }
}