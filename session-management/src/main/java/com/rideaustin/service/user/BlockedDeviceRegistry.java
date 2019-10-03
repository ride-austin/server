package com.rideaustin.service.user;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.stereotype.Service;

import com.rideaustin.repo.dsl.BlockedDeviceDslRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
public class BlockedDeviceRegistry {

  private static final Set<String> BLOCKED_DEVICES = ConcurrentHashMap.newKeySet();

  private final BlockedDeviceDslRepository repository;

  @Inject
  public BlockedDeviceRegistry(BlockedDeviceDslRepository repository) {
    this.repository = repository;
  }

  @PostConstruct
  public void refresh() {
    Collection<String> blockedDevices = repository.findAllIds();
    BLOCKED_DEVICES.clear();
    if (!blockedDevices.isEmpty()) {
      BLOCKED_DEVICES.addAll(blockedDevices);
    }
  }

  public boolean isBlocked(String deviceId) {
    return BLOCKED_DEVICES.contains(deviceId);
  }

  public void handleMessage(BlockedDeviceMessage blockedDeviceMessage) {
    if (blockedDeviceMessage.type == BlockedDeviceMessage.Type.BLOCK) {
      BLOCKED_DEVICES.addAll(blockedDeviceMessage.getDeviceIds());
    } else if (blockedDeviceMessage.type == BlockedDeviceMessage.Type.UNBLOCK) {
      BLOCKED_DEVICES.removeAll(blockedDeviceMessage.getDeviceIds());
    }
  }

  @Getter
  @RequiredArgsConstructor
  public static class BlockedDeviceMessage implements Serializable {

    private static final long serialVersionUID = 8698659452633629845L;

    private final Collection<String> deviceIds;
    private final Type type;

    public enum Type {
      BLOCK,
      UNBLOCK
    }
  }

}
