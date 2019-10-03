package com.rideaustin.model.helper;

import java.math.BigDecimal;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import com.rideaustin.Constants;

@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {
  @Override
  public BigDecimal convertToDatabaseColumn(final Money attribute) {
    return attribute == null ? null : attribute.getAmount();
  }

  @Override
  public Money convertToEntityAttribute(final BigDecimal dbData) {
    if (dbData != null) {
      return Money.of(CurrencyUnit.USD, dbData.setScale(CurrencyUnit.USD.getDecimalPlaces(), Constants.ROUNDING_MODE));
    }

    return null;
  }
}
