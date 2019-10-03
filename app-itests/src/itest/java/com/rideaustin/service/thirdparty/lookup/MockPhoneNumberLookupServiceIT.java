package com.rideaustin.service.thirdparty.lookup;

import static org.assertj.core.api.Assertions.assertThat;

import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.rest.exception.ServerError;

@WebAppConfiguration
@ActiveProfiles({"dev","itest"})
@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(properties = {"lookup.api.default.provider=mock"})
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class}, initializers = RAApplicationInitializer.class)
public class MockPhoneNumberLookupServiceIT {

  @Inject
  private PhoneNumberLookupService phoneNumberLookupService;

  @Test
  public void shouldLoadMockService(){
    assertThat(phoneNumberLookupService.getClass()).isEqualTo(MockPhoneNumberLookupService.class);
  }

  @Test
  public void shouldReturnPhoneNumberInfo() throws ServerError {
    final String phoneNumber = "+15555555555";

    PhoneNumberInfo info = phoneNumberLookupService.lookup(phoneNumber);

    assertThat(info).isNotNull();
    assertThat(info.getRawNumber()).isEqualTo(phoneNumber);
    assertThat(info.getCountryCode()).isEqualTo("US");
    assertThat(info.getType()).isEqualTo(PhoneNumberInfo.PhoneNumberType.MOBILE);
    assertThat(info.getStatus()).isEqualTo(PhoneNumberInfo.PhoneNumberStatus.EXISTENT);
  }
}
