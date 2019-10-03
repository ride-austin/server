package com.rideaustin.service.promocodes;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.google.common.collect.ImmutableSet;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.model.user.Rider;
import com.rideaustin.rest.model.PromocodeDto;
import com.rideaustin.testrail.TestCases;
import com.rideaustin.utils.RandomString;

@Category(RiderPromocode.class)
public class C1177048IT extends AbstractPromocodeTest {

  @Before
  public void setUp() {
    administrator = administratorFixture.getFixture();
  }

  @Test
  @TestCases("C1177048")
  public void shouldUsePromocode() throws Exception {
    final PromocodeDto promocode = newPromocode(0, 5, 5);
    administratorAction.addPromocode(administrator.getEmail(), promocode)
      .andExpect(status().isOk());
    Rider rider = riderFixtureProvider.create().getFixture();

    riderAction.usePromocode(rider,
      Promocode.builder().codeLiteral(promocode.getCodeLiteral()).build())
      .andExpect(status().isOk());
  }

  @Test
  @TestCases("C1177048")
  public void shouldNotUsePromocode_WhenStartDatePassNotPassed() throws Exception {
    final PromocodeDto promocode = newPromocode(5, 5, 5);
    administratorAction.addPromocode(administrator.getEmail(), promocode)
      .andExpect(status().isOk());

    Rider rider = riderFixtureProvider.create().getFixture();
    riderAction.usePromocode(rider,
      Promocode.builder().codeLiteral(promocode.getCodeLiteral()).build())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString(PromocodeService.INVALID_PROMOCODE)));
  }

  @Test
  @TestCases("C1177048")
  public void shouldNotUsePromocode_WhenEndDatePassed() throws Exception {
    final PromocodeDto promocode = newPromocode(-10, -5, 5);
    administratorAction.addPromocode(administrator.getEmail(), promocode)
      .andExpect(status().isOk());

    Rider rider = riderFixtureProvider.create().getFixture();
    riderAction.usePromocode(rider,
      Promocode.builder().codeLiteral(promocode.getCodeLiteral()).build())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString(PromocodeService.INVALID_PROMOCODE)));
  }

  @Test
  @TestCases("C1177048")
  public void shouldNotUsePromocode_WhenStartDateIsAfterEndDate() throws Exception {
    final PromocodeDto promocode = newPromocode(0, -5, 5);
    administratorAction.addPromocode(administrator.getEmail(), promocode)
      .andExpect(status().isBadRequest());
  }

  @Test
  @TestCases("C1177048")
  public void shouldNotUsePromocode_WhenUseEndDatePassed() throws Exception {
    final PromocodeDto promocode = newPromocode(0, 5, -5);
    administratorAction.addPromocode(administrator.getEmail(), promocode)
      .andExpect(status().isOk());

    Rider rider = riderFixtureProvider.create().getFixture();
    riderAction.usePromocode(rider,
      Promocode.builder().codeLiteral(promocode.getCodeLiteral()).build())
      .andExpect(status().isBadRequest())
      .andExpect(content().string(containsString(PromocodeService.INVALID_PROMOCODE)));
  }

  private PromocodeDto newPromocode(int startDif, int endDif, int useDif) {
    Date now = new Date();
    return PromocodeDto.builder()
      .codeValue(BigDecimal.TEN)
      .codeLiteral(RandomString.generate(6))
      .startsOn(DateUtils.addDays(now, startDif))
      .cities(ImmutableSet.of(1L))
      .carTypes(ImmutableSet.of("REGULAR"))
      .endsOn(DateUtils.addDays(now, endDif))
      .useEndDate(DateUtils.addDays(now, useDif))
      .build();
  }
}
