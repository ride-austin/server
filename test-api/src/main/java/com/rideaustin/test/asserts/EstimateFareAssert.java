package com.rideaustin.test.asserts;

import org.assertj.core.api.AbstractAssert;

import com.rideaustin.test.response.EstimateFareDto;

public class EstimateFareAssert extends AbstractAssert<EstimateFareAssert, EstimateFareDto> {

  private EstimateFareAssert(EstimateFareDto estimatedFareDTO) {
    super(estimatedFareDTO, EstimateFareAssert.class);
  }

  public static EstimateFareAssert assertThat(EstimateFareDto initial) {
    return new EstimateFareAssert(initial);
  }

  public EstimateFareAssert surgeFactorUpgradeHasAnEffect(EstimateFareDto updatedEstimation) {
    isNotNull();
    if ((actual.getDuration() != updatedEstimation.getDuration())
      && (actual.getTotalFare() <= updatedEstimation.getTotalFare())) {
      failWithMessage("Pre charge should not be used");
    }
    return this;
  }

}
