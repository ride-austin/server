package com.rideaustin.service.promocodes;

import java.math.BigDecimal;

import com.rideaustin.model.promocodes.PromocodeRedemption;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PromocodeUseResult {

  private boolean success;
  private BigDecimal promocodeCreditUsed;
  private PromocodeRedemption affectedPromocodeRedemption;

  public PromocodeUseResult() {
    this(false, BigDecimal.ZERO, null);
  }

  /**
   * For tests only
   * @param creditUsed
   */
  public PromocodeUseResult(BigDecimal creditUsed) {
    this(true, creditUsed, null);
  }

  public PromocodeUseResult(boolean success, BigDecimal promocodeCreditUsed,
    PromocodeRedemption affectedPromocodeRedemption) {
    this.success = success;
    this.promocodeCreditUsed = promocodeCreditUsed;
    this.affectedPromocodeRedemption = affectedPromocodeRedemption;
  }
}
