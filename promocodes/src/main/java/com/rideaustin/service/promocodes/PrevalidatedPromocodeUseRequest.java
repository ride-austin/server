package com.rideaustin.service.promocodes;

import static com.rideaustin.utils.SafeZeroUtils.safeZero;

import com.rideaustin.model.ride.FareDetails;
import com.rideaustin.model.ride.Ride;

public class PrevalidatedPromocodeUseRequest extends PromocodeUseRequest {

  public PrevalidatedPromocodeUseRequest(Ride ride, FareDetails fareDetails, boolean coveredByCampaign) {
    super(ride, safeZero(fareDetails.getSubTotal()).getAmount(), safeZero(fareDetails.getRideCost()).minus(safeZero(fareDetails.getCityFee())).getAmount(), coveredByCampaign);
  }

}
