package com.rideaustin.service.thirdparty.lookup;

import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberType.UNKNOWN;
import static com.rideaustin.service.thirdparty.lookup.PhoneNumberInfo.PhoneNumberType.VOIP;
import static org.junit.Assert.assertEquals;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class PhoneNumberTypeTest {

  @Test
  public void shouldAccept_WhenValidValueIsGiven() {
    final String value = "Voip";

    PhoneNumberInfo.PhoneNumberType actual = PhoneNumberInfo.PhoneNumberType.fromValue(value);

    assertEquals(VOIP, actual);
  }

  @Test
  public void shouldAccept_WhenValidValueIsGiven_AsUppercase() {
    final String value = "VOIP";

    PhoneNumberInfo.PhoneNumberType actual = PhoneNumberInfo.PhoneNumberType.fromValue(value);

    assertEquals(VOIP, actual);
  }

  @Test
  public void shouldAccept_WhenValidValueIsGiven_AsLowerCase() {
    final String value = "voip";

    PhoneNumberInfo.PhoneNumberType actual = PhoneNumberInfo.PhoneNumberType.fromValue(value);

    assertEquals(VOIP, actual);
  }

  @Test
  public void shouldReturnUnknown_WhenInvalidValueIsGiven() {
    final String value = null;

    PhoneNumberInfo.PhoneNumberType actual = PhoneNumberInfo.PhoneNumberType.fromValue(value);

    assertEquals(UNKNOWN, actual);
  }

  @Test
  public void shouldReturnUnknown_WhenEmptyValueIsGiven() {
    final String value = StringUtils.EMPTY;

    PhoneNumberInfo.PhoneNumberType actual = PhoneNumberInfo.PhoneNumberType.fromValue(value);

    assertEquals(UNKNOWN, actual);
  }

  @Test
  public void shouldReturnUnknown_WhenBlankValueIsGiven() {
    final String value = "   ";

    PhoneNumberInfo.PhoneNumberType actual = PhoneNumberInfo.PhoneNumberType.fromValue(value);

    assertEquals(UNKNOWN, actual);
  }

}
