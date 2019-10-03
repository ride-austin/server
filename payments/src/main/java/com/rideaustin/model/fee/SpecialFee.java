package com.rideaustin.model.fee;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Special fee to be applied to ride cost.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class SpecialFee {

  public static final String TITLE_AIRPORT_PICKUP_SURCHARGE = "AIRPORT PICKUP SURCHARGE";

  public enum ValueType {
    AMOUNT,
    RATE
  }

  private String title;
  private String description;
  private ValueType valueType;
  private BigDecimal value;
}
