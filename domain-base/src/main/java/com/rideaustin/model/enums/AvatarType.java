package com.rideaustin.model.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public enum AvatarType {
  RIDER(1, AvatarType.ROLE_RIDER, null),
  DRIVER(2, AvatarType.ROLE_DRIVER, null),
  ADMIN(4, AvatarType.ROLE_ADMIN, null),
  API_CLIENT(8, AvatarType.ROLE_API_CLIENT, null),
  DISPATCHER(16, AvatarType.ROLE_DISPATCHER, AvatarType.RIDER);

  private final int bitMask;
  private final String roleName;
  private final AvatarType aliasTo;

  public static final String NAME_RIDER = "RIDER";
  public static final String NAME_DRIVER = "DRIVER";
  public static final String NAME_ADMIN = "ADMIN";
  public static final String NAME_API_CLIENT = "API_CLIENT";

  public static final String ROLE_RIDER = "ROLE_RIDER";
  public static final String ROLE_DRIVER = "ROLE_DRIVER";
  public static final String ROLE_ADMIN = "ROLE_ADMIN";
  public static final String ROLE_API_CLIENT = "ROLE_API_CLIENT";
  public static final String ROLE_DISPATCHER = "ROLE_DISPATCHER";

  public static final String MAPPING_ADMIN = "avatarType=ADMIN";
  public static final String MAPPING_API_CLIENT = "avatarType=API_CLIENT";
  public static final String MAPPING_RIDER = "avatarType=RIDER";
  public static final String MAPPING_DRIVER = "avatarType=DRIVER";
  public static final String MAPPING_DISPATCHER = "avatarType=DISPATCHER";

  private static final Map<Integer, AvatarType> BY_MASK = new HashMap<>();

  static {
    for (AvatarType at : AvatarType.values()) {
      int mask = at.toBitMask();
      BY_MASK.put(mask, at);
    }
  }

  AvatarType(int bitMask, String roleName, AvatarType aliasTo) {
    this.bitMask = bitMask;
    this.roleName = roleName;
    this.aliasTo = aliasTo;
  }

  public int toBitMask() {
    return bitMask;
  }

  public String roleName() {
    return roleName;
  }

  public static Set<AvatarType> fromBitMask(final int bitMask) {
    Set<AvatarType> ret = EnumSet.noneOf(AvatarType.class);

    int bitmaskCopy = bitMask;
    while (bitmaskCopy != 0) {
      int curr = Integer.lowestOneBit(bitmaskCopy);
      ret.add(BY_MASK.get(curr));
      bitmaskCopy ^= curr;
    }

    return ret;
  }

  public AvatarType resolveAlias() {
    return Optional.ofNullable(aliasTo).orElse(this);
  }

}
