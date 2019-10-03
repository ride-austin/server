package com.rideaustin.service.promocodes;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.lang3.time.DateUtils;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.promocodes.PromocodeRedemption;
import com.rideaustin.model.user.Rider;
import com.rideaustin.repo.dsl.PromocodeRedemptionDslRepository;
import com.rideaustin.service.generic.TimeService;

public abstract class PromocodeRedemptionPolicy {

  private final TimeService timeService;
  private final PromocodeRedemptionDslRepository promocodeRedemptionDslRepository;

  protected boolean dryRun;

  public PromocodeRedemptionPolicy(TimeService timeService, PromocodeRedemptionDslRepository promocodeRedemptionDslRepository) {
    this.timeService = timeService;
    this.promocodeRedemptionDslRepository = promocodeRedemptionDslRepository;
  }

  public void applyPromocode(Rider rider, Promocode promocode) throws PromocodeException {
    createRedemption(rider, promocode);
  }

  public PromocodeUseResult useRedemption(PromocodeUseRequest request, PromocodeRedemption promocodeRedemption) {
    BigDecimal freeCreditUsed = getFreeCreditUsed(request, promocodeRedemption);
    if (dryRun) {
      return new PromocodeUseResult(true, freeCreditUsed, null);
    } else {
      BigDecimal remainingValueDecrement = getRemainingValueDecrement(freeCreditUsed, promocodeRedemption);
      promocodeRedemption.decreaseRemainingValue(remainingValueDecrement);
      promocodeRedemption.increaseNumberOfTimesUsed();
      final Promocode promocode = promocodeRedemption.getPromocode();
      if ((promocode.getMaximumUsesPerAccount() != null &&
        promocodeRedemption.getNumberOfTimesUsed().compareTo(promocode.getMaximumUsesPerAccount()) > -1)
        || promocodeRedemption.getRemainingValue().compareTo(BigDecimal.ZERO) < 1 || promocode.isNextTripOnly()) {
        promocodeRedemption.setActive(false);
      }
      if (promocode.isNewRidersOnly()) {
        final List<PromocodeRedemption> otherNewRiderRedemptions = promocodeRedemptionDslRepository
          .findNewRiderRedemptions(request.getRiderId(), promocodeRedemption);
        otherNewRiderRedemptions.forEach(r -> r.setActive(false));
        promocodeRedemptionDslRepository.saveMany(otherNewRiderRedemptions);
      }
      PromocodeRedemption savedRedemption = promocodeRedemptionDslRepository.save(promocodeRedemption);
      return new PromocodeUseResult(true, freeCreditUsed, savedRedemption);
    }
  }

  private BigDecimal getRemainingValueDecrement(BigDecimal freeCreditUsed, PromocodeRedemption promocodeRedemption) {
    Promocode promocode = promocodeRedemption.getPromocode();
    if (promocode.getCappedAmountPerUse() != null && freeCreditUsed.compareTo(promocode.getCappedAmountPerUse()) < 0) {
      return promocode.getCappedAmountPerUse();
    }
    return freeCreditUsed;
  }

  void createRedemption(Rider rider, Promocode promocode) {
    PromocodeRedemption promocodeRedemption = new PromocodeRedemption();
    promocodeRedemption.setPromocode(promocode);
    promocodeRedemption.setRider(rider);
    promocodeRedemption.setActive(true);
    promocodeRedemption.setOriginalValue(promocode.getCodeValue());
    promocodeRedemption.setRemainingValue(promocode.getCodeValue());
    if (promocode.getUseEndDate() != null) {
      promocodeRedemption.setValidUntil(promocode.getUseEndDate());
    } else if (promocode.getValidForNumberOfDays() != null) {
      promocodeRedemption.setValidUntil(DateUtils.addDays(timeService.getCurrentDate(), promocode.getValidForNumberOfDays()));
    }
    promocodeRedemptionDslRepository.save(promocodeRedemption);
  }

  private BigDecimal getFreeCreditUsed(PromocodeUseRequest request, PromocodeRedemption promocodeRedemption) {
    BigDecimal freeCreditUsed;
    Promocode promocode = promocodeRedemption.getPromocode();
    BigDecimal requestedAmount = promocode.isApplicableToFees()
      ? request.getRideCreditAmount()
      : request.getFareCreditAmount();
    if (promocode.getCappedAmountPerUse() != null && requestedAmount.compareTo(promocode.getCappedAmountPerUse()) > 0) {
      requestedAmount = promocode.getCappedAmountPerUse();
    }
    if (promocodeRedemption.getRemainingValue().compareTo(requestedAmount) > -1) {
      freeCreditUsed = requestedAmount;
    } else {
      freeCreditUsed = promocodeRedemption.getRemainingValue();
    }
    return freeCreditUsed;
  }

  public PromocodeRedemptionPolicy dryRun(boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }
}
