package com.rideaustin.model.promocodes;

import com.rideaustin.service.promocodes.PromocodeRedemptionPolicy;
import com.rideaustin.service.promocodes.PublicPromocodeRedemptionPolicy;
import com.rideaustin.service.promocodes.UserPromocodeRedemptionPolicy;

public enum PromocodeType {
  USER(UserPromocodeRedemptionPolicy.class),
  PUBLIC(PublicPromocodeRedemptionPolicy.class);

  private Class<? extends PromocodeRedemptionPolicy> promocodeFreeCreditPolicy;

  PromocodeType(Class<? extends PromocodeRedemptionPolicy> promocodeFreeCreditPolicy) {
    this.promocodeFreeCreditPolicy = promocodeFreeCreditPolicy;
  }

  public Class<? extends PromocodeRedemptionPolicy> getFreeCreditPolicy() {
    return promocodeFreeCreditPolicy;
  }
}
