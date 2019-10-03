package com.rideaustin.assemblers;

import org.springframework.stereotype.Component;

import com.rideaustin.model.promocodes.Promocode;
import com.rideaustin.rest.model.RiderPromoCodeDto;

@Component
public class RiderPromocodeDtoAssembler implements SingleSideAssembler<Promocode, RiderPromoCodeDto> {

  private static final String DETAIL_TEXT_MESSAGE = "Every time a new user signs up with your invite code, " +
    "they'll receive their first ride free. Once they take their first ride, you'll automatically get $%s credited " +
    "into your account (up to $500)";

  private static final String SMS_BODY_MESSAGE = "You should try App! Get $%s in ride credit using my code ​'%s'​. " +
    "Download the app at: ";

  private static final String EMAIL_BODY_MESSAGE = "<p>You should try App! Get $%s in ride credit using my code ​" +
    "<b>%s</b>. Download the app at: </p>";

  @Override
  public RiderPromoCodeDto toDto(Promocode promocode) {
    String promocodeValue = "0";
    if (promocode.getCodeValue() != null) {
      promocodeValue = promocode.getCodeValue().stripTrailingZeros().toPlainString();
    }
    return RiderPromoCodeDto.builder()
      .codeLiteral(promocode.getCodeLiteral())
      .codeValue(promocode.getCodeValue())
      .maximumRedemption(promocode.getMaximumRedemption())
      .currentRedemption(promocode.getCurrentRedemption())
      .detailText(String.format(DETAIL_TEXT_MESSAGE, promocodeValue))
      .smsBody(String.format(SMS_BODY_MESSAGE, promocodeValue, promocode.getCodeLiteral()))
      .emailBody(String.format(EMAIL_BODY_MESSAGE, promocodeValue, promocode.getCodeLiteral()))
      .build();
  }
}
