package com.rideaustin.service.promocodes;

import java.util.Objects;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.service.generic.TimeService;

@Component
public class UserPromocodeRedemptionPolicy extends PromocodeRedemptionPolicy {

  @Inject
  public UserPromocodeRedemptionPolicy(TimeService timeService, PromocodeRedemptionDslRepository promocodeRedemptionDslRepository) {
    super(timeService, promocodeRedemptionDslRepository);
  }

  @Override
  public void applyPromocode(Rider rider, Promocode promocode) throws PromocodeException {
    if (rider.equals(promocode.getOwner())) {
      throw new PromocodeException("Invalid code");
    }
    super.applyPromocode(rider, promocode);
  }

  @Override
  public PromocodeUseResult useRedemption(PromocodeUseRequest request, PromocodeRedemption promocodeRedemption) {
    if (!dryRun) {
      Promocode promocode = promocodeRedemption.getPromocode();
      if (!isOwner(promocode, request) && !promocodeRedemption.isAppliedToOwner()) {
        promocodeRedemption.setAppliedToOwner(true);
        createRedemption(promocode.getOwner(), promocodeRedemption.getPromocode());
      }
    }
    return super.useRedemption(request, promocodeRedemption);

  }

  private boolean isOwner(Promocode promocode, PromocodeUseRequest request) {
    return Objects.equals(promocode.getOwner().getId(), request.getRiderId());
  }
}
