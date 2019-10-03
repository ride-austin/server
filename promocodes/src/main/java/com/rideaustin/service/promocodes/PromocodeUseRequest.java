package com.rideaustin.service.promocodes;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import java.math.BigDecimal;

import com.rideaustin.model.enums.RideStatus;
import com.rideaustin.model.ride.Ride;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class PromocodeUseRequest {
  private final Long riderId;
  private final Long cityId;
  private final Long rideId;
  private final String carCategory;
  private final BigDecimal fareCreditAmount;
  private final BigDecimal rideCreditAmount;
  private final Long redemptionId;
  private final boolean valid;
  private final boolean coveredByCampaign;

  protected PromocodeUseRequest(Ride ride, BigDecimal fareCreditAmount, BigDecimal rideCreditAmount, boolean coveredByCampaign) {
    this.riderId = ride.getRider().getId();
    this.cityId = ride.getCityId();
    this.carCategory = ride.getRequestedCarType().getCarCategory();
    this.fareCreditAmount = fareCreditAmount;
    this.rideCreditAmount = rideCreditAmount;
    this.coveredByCampaign = coveredByCampaign;
    this.rideId = ride.getId();
    this.valid = true;
    this.redemptionId = null;
  }

  public PromocodeUseRequest(Ride ride, boolean coveredByCampaign) {
    this(ride, ride.getRider().getId(), coveredByCampaign);
  }

  public PromocodeUseRequest(Ride ride, Long riderId, boolean coveredByCampaign) {
    this.riderId = riderId;
    this.cityId = ride.getCityId();
    this.rideId = ride.getId();
    this.carCategory = ride.getRequestedCarType().getCarCategory();
    this.fareCreditAmount = safeZero(ride.getSubTotal()).getAmount();
    this.rideCreditAmount = safeZero(ride.getRideCost()).minus(safeZero(ride.getCityFee())).getAmount();
    this.redemptionId = ride.getPromocodeRedemptionId();
    this.valid = ride.getStatus() == RideStatus.COMPLETED;
    this.coveredByCampaign = coveredByCampaign;
  }

  public PromocodeUseRequest(Long riderId, Long cityId, String carCategory, BigDecimal fareCreditAmount, BigDecimal rideCreditAmount, boolean coveredByCampaign) {
    this.riderId = riderId;
    this.cityId = cityId;
    this.carCategory = carCategory;
    this.fareCreditAmount = fareCreditAmount;
    this.rideCreditAmount = rideCreditAmount;
    this.coveredByCampaign = coveredByCampaign;
    this.rideId = null;
    this.valid = true;
    this.redemptionId = null;
  }
}