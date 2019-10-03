package com.rideaustin.service.promocodes;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Date;

import javax.inject.Inject;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.PromocodeDto;
import com.rideaustin.testrail.TestCases;
import com.rideaustin.utils.RandomString;

@Category(RiderPromocode.class)
public class C1177050IT extends AbstractPromocodeTest {

  @Inject
  private PromocodeServiceConfig promocodeServiceConfig;

  @Test
  @TestCases("C1177050")
  public void shouldNotAddPromocode_WhenLimitIsExceeded() throws Exception {
    administrator = administratorFixture.getFixture();
    Rider rider = riderFixtureProvider.create().getFixture();
    BigDecimal maxActiveRedeemedCredit = promocodeServiceConfig.getMaximumActiveRedeemedCredit();
    BigDecimal maxPromocodeValue = promocodeServiceConfig.getMaximumPromocodeValue();

    addPromocodes(maxActiveRedeemedCredit, maxPromocodeValue, rider);

    final PromocodeDto lastPromocode = newPromocode(maxPromocodeValue, 0, 5, 5);
    administratorAction.addPromocode(administrator.getEmail(), lastPromocode)
      .andExpect(status().isOk());

    riderAction.usePromocode(rider,
      Promocode.builder().codeLiteral(lastPromocode.getCodeLiteral()).build())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString(String.format("Maximum allowed credit ($%s) exceeded", maxActiveRedeemedCredit))));
  }

  private void addPromocodes(BigDecimal maxActiveRedeemedCredit, BigDecimal maxPromocodeValue, Rider rider) throws Exception {
    BigDecimal count = BigDecimal.ZERO;
    BigDecimal maxperPromo = maxPromocodeValue;
    BigDecimal limit = maxActiveRedeemedCredit.subtract(BigDecimal.ONE);
    while (count.compareTo(limit) < 0) {
      PromocodeDto promocodeDto = newPromocode(maxperPromo, 0, 5, 5);
      administratorAction.addPromocode(administrator.getEmail(), promocodeDto)
        .andExpect(status().isOk());

      riderAction.usePromocode(rider,
        Promocode.builder().codeLiteral(promocodeDto.getCodeLiteral()).build())
        .andExpect(status().isOk());

      count = count.add(maxperPromo);
      maxperPromo = maxPromocodeValue.min(limit.subtract(count));
    }
  }

  private PromocodeDto newPromocode(BigDecimal value, int startDif, int endDif, int useDif) {
    Date now = new Date();
    return PromocodeDto.builder()
      .codeValue(value)
      .codeLiteral(RandomString.generate(6))
      .startsOn(DateUtils.addDays(now, startDif))
      .cities(ImmutableSet.of(1L))
      .carTypes(ImmutableSet.of("REGULAR"))
      .endsOn(DateUtils.addDays(now, endDif))
      .useEndDate(DateUtils.addDays(now, useDif))
      .build();
  }
}
