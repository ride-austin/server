package com.rideaustin.service.thirdparty.lookup;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.rideaustin.config.AppConfig;
import com.rideaustin.config.RAApplicationInitializer;
import com.rideaustin.config.WebConfig;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.test.common.ITestProfileSupport;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {AppConfig.class, WebConfig.class}, initializers = RAApplicationInitializer.class)
@WebAppConfiguration
public class TwilioPhoneNumberLookupServiceIT extends ITestProfileSupport {

  @Inject
  private PhoneNumberLookupService phoneNumberLookupService;

  @Test
  public void shouldDetect_MobilePhoneNumber() throws ServerError {
    final String mobileNumber = "+905544551212";

    PhoneNumberInfo info = phoneNumberLookupService.lookup(mobileNumber);

    Assert.assertEquals(PhoneNumberInfo.PhoneNumberType.MOBILE, info.getType());
  }

  @Test
  public void shouldDetect_GoogleVoiceNumber_ThatNeedsUrlEncoding() throws ServerError {
    final String googleVoiceNumber = "(646) 470-2534";

    PhoneNumberInfo info = phoneNumberLookupService.lookup(googleVoiceNumber);

    Assert.assertEquals(PhoneNumberInfo.PhoneNumberType.VOIP, info.getType());
  }

  @Test
  public void shouldDetect_GoogleVoiceNumber_ThatDoesNotUrlEncoding() throws ServerError {
    final String googleVoiceNumber = "6464702534";

    PhoneNumberInfo info = phoneNumberLookupService.lookup(googleVoiceNumber);

    Assert.assertEquals(PhoneNumberInfo.PhoneNumberType.VOIP, info.getType());
  }

  @Test
  public void shouldDetect_GoogleVoiceNumber_InNationalFormat() throws ServerError {
    final String googleVoiceNumber = "(646) 470-2534";

    PhoneNumberInfo info = phoneNumberLookupService.lookup(googleVoiceNumber);

    Assert.assertEquals(PhoneNumberInfo.PhoneNumberType.VOIP, info.getType());
  }

  @Test
  public void shouldDetect_GoogleVoiceNumber_InE164Format() throws ServerError {
    final String googleVoiceNumber = "+16464702534";

    PhoneNumberInfo info = phoneNumberLookupService.lookup(googleVoiceNumber);

    Assert.assertEquals(PhoneNumberInfo.PhoneNumberType.VOIP, info.getType());
  }

  @Test
  public void shouldHandle_NonExistentPhoneNumber() throws ServerError {
    final String mobileNumber = "+15555555555";

    PhoneNumberInfo info = phoneNumberLookupService.lookup(mobileNumber);

    Assert.assertEquals(PhoneNumberInfo.PhoneNumberType.UNKNOWN, info.getType());
    Assert.assertEquals(PhoneNumberInfo.PhoneNumberStatus.NON_EXISTENT, info.getStatus());
  }
}
