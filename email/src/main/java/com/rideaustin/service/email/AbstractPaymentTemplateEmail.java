package com.rideaustin.service.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.rideaustin.model.enums.CardBrand;
import com.rideaustin.model.enums.PaymentProvider;
import com.rideaustin.model.enums.PaymentStatus;
import com.rideaustin.rest.model.RiderCardDto;
import com.rideaustin.service.model.FarePaymentDto;

public abstract class AbstractPaymentTemplateEmail extends AbstractTemplateEmail {

  public AbstractPaymentTemplateEmail(String subject, String template) {
    super(subject, template);
  }

  protected void insertPaymentMethodInformation(Map<String, Object> dataModel, FarePaymentDto riderPayment) {
    PaymentInformation info = null;
    if (riderPayment.getPaymentStatus() == PaymentStatus.PAID) {
      if (riderPayment.getPaymentProvider() == PaymentProvider.APPLE_PAY) {
        info = new PaymentInformation(riderPayment.getPaymentProvider().getIcon(),
          riderPayment.getPaymentProvider().getName(), "", "");
      } else if (riderPayment.getPaymentProvider() == PaymentProvider.CREDIT_CARD) {
        info = createCCPaymentInfo(riderPayment);
      }
    } else {
      info = new PaymentInformation(CardBrand.VISA.imageURL(), "", "", "No charge");
    }

    dataModel.putAll(info);
  }

  private PaymentInformation createCCPaymentInfo(FarePaymentDto riderPayment) {
    Optional<RiderCardDto> usedCard = Optional.ofNullable(riderPayment.getUsedCard());
    Optional<CardBrand> cardBrand = usedCard.map(RiderCardDto::getCardBrand);
    return new PaymentInformation(cardBrand.map(CardBrand::imageURL).orElse(null),
      cardBrand.map(CardBrand::name).orElse(null), "Personal",
      usedCard.map(RiderCardDto::getCardNumber).map("&#x25CF;&#x25CF;&#x25CF;&#x25CF;"::concat).orElse(null));
  }

  private static class PaymentInformation extends HashMap<String, String> {
    PaymentInformation(String image, String brand, String nickname, String number) {
      put("cardImage", image);
      put("cardBrand", brand);
      put("cardNickName", nickname);
      put("cardNumber", number);
    }
  }
}
