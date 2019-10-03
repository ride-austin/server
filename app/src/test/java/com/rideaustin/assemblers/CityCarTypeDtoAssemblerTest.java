package com.rideaustin.assemblers;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Date;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.rideaustin.model.ride.CarType;
import com.rideaustin.model.ride.CityCarType;
import com.rideaustin.rest.model.CityCarTypeDto;
import com.rideaustin.test.util.TestUtils;

public class CityCarTypeDtoAssemblerTest {

  @Test
  public void shouldMapToDto() {
    // given
    CarType carType = new CarType("carCategory", "title", "description", "iconUrl", "plainIconUrl", "mapIconUrl",
      "fullUrl", "selUrl", "unselUrl", "femUrl", "{\"parent\":true}", randomInt(), randomInt(), randomInt(), true, Sets.newHashSet(new CityCarType()));
    CityCarType cityCarType = new CityCarType(2L, carType, true, "{\"child\":true}", randomMoney(), randomMoney(),
      randomMoney(), randomMoney(), randomMoney(), randomMoney(), randomBigDecimal(),
      randomBigDecimal(), randomBigDecimal(), randomMoney(), "processingFeeText", randomMoney());
    cityCarType.setId(2L);
    cityCarType.setUpdatedDate(new Date());
    cityCarType.setCreatedDate(new Date());

    // when
    ObjectMapper mapper = new ObjectMapper();
    CityCarTypeDto result = new CityCarTypeDtoAssembler(mapper).toDto(cityCarType);

    // then
    assertEquals(cityCarType.getCityId(), result.getCityId());
    assertEquals(cityCarType.getCarType().getActive(), result.getActive());
    assertEquals(cityCarType.getBaseFare(), result.getBaseFare());
    assertEquals(cityCarType.getBookingFee(), result.getBookingFee());
    assertEquals(cityCarType.getCancellationFee(), result.getCancellationFee());
    assertEquals(cityCarType.getCarType().getCarCategory(), result.getCarCategory());
    assertEquals("{\"parent\":true,\"child\":true}", result.getConfiguration());
    assertEquals(cityCarType.getCarType().getDescription(), result.getDescription());
    assertEquals(cityCarType.getCarType().getIconUrl(), result.getIconUrl());
    assertEquals(cityCarType.getCarType().getPlainIconUrl(), result.getPlainIconUrl());
    assertEquals(cityCarType.getCarType().getFullIconUrl(), result.getFullIconUrl());
    assertEquals(cityCarType.getCarType().getMaxPersons(), result.getMaxPersons());
    assertEquals(cityCarType.getMinimumFare(), result.getMinimumFare());
    assertEquals(cityCarType.getCarType().getOrder(), result.getOrder());
    assertEquals(cityCarType.getProcessingFeeRate(), result.getProcessingFeeRate());
    assertEquals(cityCarType.getProcessingFeeText(), result.getProcessingFeeText());
    assertEquals(cityCarType.getFixedRAFee(), result.getRaFixedFee());
    assertEquals(cityCarType.getRatePerMile(), result.getRatePerMile());
    assertEquals(cityCarType.getRatePerMinute(), result.getRatePerMinute());
    assertEquals(cityCarType.getCarType().getTitle(), result.getTitle());
  }

  private Integer randomInt() {
    return TestUtils.RANDOM.nextInt();
  }

  private BigDecimal randomBigDecimal() {
    return BigDecimal.valueOf(TestUtils.RANDOM.nextDouble() * 5);
  }

  private Money randomMoney() {
    return Money.ofMajor(CurrencyUnit.USD, TestUtils.RANDOM.nextLong());
  }
}