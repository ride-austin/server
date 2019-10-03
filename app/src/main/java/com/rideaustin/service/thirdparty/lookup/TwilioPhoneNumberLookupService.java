package com.rideaustin.service.thirdparty.lookup;

import java.io.UnsupportedEncodingException;

import javax.inject.Inject;

import org.apache.commons.codec.net.URLCodec;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import com.rideaustin.Constants;
import com.rideaustin.rest.exception.ServerError;
import com.rideaustin.service.thirdparty.lookup.condition.TwilioLookupServiceCondition;
import com.twilio.exception.ApiConnectionException;
import com.twilio.exception.ApiException;
import com.twilio.http.TwilioRestClient;
import com.twilio.rest.lookups.v1.PhoneNumber;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Conditional(TwilioLookupServiceCondition.class)
@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class TwilioPhoneNumberLookupService implements PhoneNumberLookupService {

  private static final String PHONE_NUMBER_NOT_FOUND_REGEX = "The requested resource .* was not found";

  private static final URLCodec URL_CODEC = new URLCodec();

  private final TwilioRestClient twilioRestClient;

  @Override
  public PhoneNumberInfo lookup(String phoneNumber) throws ServerError {
    final String encodedPhoneNumber = encode(phoneNumber);

    PhoneNumberInfo result;
    try {
      PhoneNumber number = PhoneNumber.fetcher(new com.twilio.type.PhoneNumber(encodedPhoneNumber))
        .setType(LookupType.CARRIER.getName())
        .fetch(twilioRestClient);
      String typeString = number.getCarrier().get(CarrierInfo.TYPE.getName());
      PhoneNumberInfo.PhoneNumberType type = PhoneNumberInfo.PhoneNumberType.fromValue(typeString);
      result = new PhoneNumberInfo(phoneNumber, number.getCountryCode(), type, PhoneNumberInfo.PhoneNumberStatus.EXISTENT);
    } catch (ApiConnectionException connectionException) {
      log.error("Could not connect Twilio lookup service", connectionException);
      throw new PhoneNumberLookupException(connectionException.getMessage());
    } catch (ApiException apiException) {
      result = handleApiException(apiException, phoneNumber);
    }

    return result;
  }

  private PhoneNumberInfo handleApiException(ApiException exception, String phoneNumber) throws PhoneNumberLookupException {
    log.error("Exception occurred during Twilio lookup", exception);
    if (exception.getMessage().matches(PHONE_NUMBER_NOT_FOUND_REGEX)) {
      return new PhoneNumberInfo(phoneNumber, null, PhoneNumberInfo.PhoneNumberType.UNKNOWN, PhoneNumberInfo.PhoneNumberStatus.NON_EXISTENT);
    }

    throw new PhoneNumberLookupException(exception.getMessage());
  }

  private String encode(String phoneNumber) {
    try {
      return URL_CODEC.encode(phoneNumber, Constants.ENCODING_UTF8).replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      throw new IllegalArgumentException("Error occurred during encoding.", e);
    }
  }

  private enum LookupType {
    CARRIER("carrier"), CALLER_NAME("caller-name");

    private final String name;

    LookupType(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }

  private enum CarrierInfo {
    MOBILE_COUNTRY_CODE("mobile_country_code"),
    MOBILE_NETWORK_CODE("mobile_network_code"),
    NAME("name"),
    TYPE("type");

    private final String name;

    CarrierInfo(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }
  }
}
