package com.rideaustin.rest.assembler;

import com.rideaustin.assemblers.RiderPromocodeDtoAssembler;
import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.rest.model.RiderPromoCodeDto;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class RiderPromocodeDtoAssemblerTest {

  private static final String DETAIL_TEXT_MESSAGE = "Every time a new user signs up with your invite code, " +
    "they'll receive their first ride free. Once they take their first ride, you'll automatically get $10 credited " +
    "into your account (up to $500)";

  private static final String SMS_BODY_MESSAGE = "You should try App! Get $10 in ride credit using my code ​'ABC'​. " +
    "Download the app at: ";

  private static final String EMAIL_BODY_MESSAGE = "<p>You should try App! Get $10 in ride credit using my code ​" +
    "<b>ABC</b>. Download the app at: </p>";

  private RiderPromocodeDtoAssembler testedInstance;

  @Before
  public void setUp() throws Exception {
    testedInstance = new RiderPromocodeDtoAssembler();
  }

  @Test
  public void toDto() throws Exception {
    Promocode promocode = new Promocode();
    String literal = "ABC";
    BigDecimal value = BigDecimal.valueOf(10.0);

    promocode.setCodeLiteral(literal);
    promocode.setCodeValue(value);
    promocode.setMaximumRedemption(20L);

    RiderPromoCodeDto riderPromoCodeDto = testedInstance.toDto(promocode);

    assertEquals(literal, riderPromoCodeDto.getCodeLiteral());
    assertEquals(value, riderPromoCodeDto.getCodeValue());
    assertEquals(DETAIL_TEXT_MESSAGE, riderPromoCodeDto.getDetailText());
    assertEquals(SMS_BODY_MESSAGE, riderPromoCodeDto.getSmsBody());
    assertEquals(EMAIL_BODY_MESSAGE, riderPromoCodeDto.getEmailBody());
  }

}