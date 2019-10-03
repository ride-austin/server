package com.rideaustin.service.user;

import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.rideaustin.events.AdminCancelledEvent;
import com.rideaustin.model.BlockedDevice;
import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.BlockedDeviceDslRepository;
import com.rideaustin.repo.dsl.RideDslRepository;
import com.rideaustin.repo.dsl.SessionDslRepository;
import com.rideaustin.service.user.BlockedDeviceRegistry.BlockedDeviceMessage;
import com.rideaustin.service.user.BlockedDeviceRegistry.BlockedDeviceMessage.Type;

@Service
public class BlockedDeviceService {

  private final BlockedDeviceDslRepository repository;
  private final SessionDslRepository sessionDslRepository;
  private final RideDslRepository rideDslRepository;
  private final ApplicationEventPublisher publisher;
  private final RedisTemplate redisTemplate;
  private final ChannelTopic blockedDevicesTopic;

  public BlockedDeviceService(BlockedDeviceDslRepository repository, SessionDslRepository sessionDslRepository,
    RideDslRepository rideDslRepository, ApplicationEventPublisher publisher,
    @Named("rideFlowRedisTemplate") RedisTemplate redisTemplate,
    @Named("blockedDevicesTopic") ChannelTopic blockedDevicesTopic) {
    this.repository = repository;
    this.sessionDslRepository = sessionDslRepository;
    this.rideDslRepository = rideDslRepository;
    this.publisher = publisher;
    this.redisTemplate = redisTemplate;
    this.blockedDevicesTopic = blockedDevicesTopic;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void addToBlackList(Rider rider) {
    List<String> allDeviceIds = sessionDslRepository.findAllDeviceIds(rider.getUser());
    List<BlockedDevice> devices = allDeviceIds.stream()
      .filter(Objects::nonNull)
      .map(id -> new BlockedDevice(rider, id))
      .collect(Collectors.toList());
    repository.saveMany(devices);
    redisTemplate.convertAndSend(blockedDevicesTopic.getTopic(), new BlockedDeviceMessage(allDeviceIds, Type.BLOCK));
    List<Ride> rides = rideDslRepository.findByRiderAndStatus(rider, EnumSet.of(RideStatus.REQUESTED, RideStatus.DRIVER_ASSIGNED, RideStatus.DRIVER_REACHED));
    for (Ride ride : rides) {
      publisher.publishEvent(new AdminCancelledEvent(this, ride.getId()));
    }
  }

  public void unblock(Rider rider) {
    List<String> ids = repository.unblock(rider);
    redisTemplate.convertAndSend(blockedDevicesTopic.getTopic(), new BlockedDeviceMessage(ids, Type.UNBLOCK));
  }

  public boolean isInBlocklist(long userId) {
    return repository.isBlocked(userId);
  }

  public boolean isInBlocklist(String deviceId) {
    return deviceId != null && repository.isBlocked(deviceId);
  }

}
