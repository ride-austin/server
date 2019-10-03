package com.rideaustin.assemblers;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.user.RiderCard;
import com.rideaustin.rest.model.RiderCardDto;

public class RiderCardDtoAssemblerTest {

  private RiderCardDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new RiderCardDtoAssembler();
  }

  @Test
  public void toDtoSkipsNull() {
    final RiderCardDto result = testedInstance.toDto((RiderCard) null);

    assertNull(result);
  }

  @Test
  public void toDtoFillsData() {
    RiderCard source = new RiderCard();
    source.setId(1L);
    source.setCardBrand(CardBrand.VISA);
    source.setCardNumber("4564");
    source.setCardExpired(false);
    source.setPrimary(true);
    source.setExpirationMonth("02");
    source.setExpirationYear("2022");

    final RiderCardDto result = testedInstance.toDto(source);

    assertEquals(source.getId(), result.getId());
    assertEquals(source.getCardBrand(), result.getCardBrand());
    assertEquals(source.getCardNumber(), result.getCardNumber());
    assertEquals(source.isCardExpired(), result.isCardExpired());
    assertEquals(source.isPrimary(), result.isPrimary());
    assertEquals(source.getExpirationMonth(), result.getExpirationMonth());
    assertEquals(source.getExpirationYear(), result.getExpirationYear());
  }
}