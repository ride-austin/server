package com.rideaustin.service.thirdparty.lookup;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.rideaustin.rest.exception.ServerError;

public class MockPhoneNumberLookupServiceTest {

  private PhoneNumberLookupService mockService = new MockPhoneNumberLookupService();

  @Test
  public void shouldReturnPhoneNumberInfo() throws ServerError {
    final String phoneNumber = "+15555555555";

    PhoneNumberInfo info = mockService.lookup(phoneNumber);

    assertThat(info).isNotNull();
    assertThat(info.getRawNumber()).isEqualTo(phoneNumber);
    assertThat(info.getCountryCode()).isEqualTo("US");
    assertThat(info.getType()).isEqualTo(PhoneNumberInfo.PhoneNumberType.MOBILE);
    assertThat(info.getStatus()).isEqualTo(PhoneNumberInfo.PhoneNumberStatus.EXISTENT);
  }
}
