package com.rideaustin.service.thirdparty;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.Ride;
import com.rideaustin.rest.exception.ServerError;

import freemarker.template.Configuration;

public interface CommunicationService {

  enum ErrorMessage {
    NOT_ONGOING_RIDE("Sorry we can't deliver your message because your ride has been finished or cancelled."),
    UNKNOWN_NUMBER("Sorry, we can't deliver your message as phone number %s is not associated with your user profile. Please check your phone number in application settings");

    private final String content;

    ErrorMessage(String content) {
      this.content = content;
    }

    public String getContent() {
      return content;
    }
  }

  enum Provider {
    AMAZON,
    TWILIO;

    public static Provider from(String value) {
      return valueOf(value.toUpperCase());
    }
  }

  @JsonFormat(shape = JsonFormat.Shape.OBJECT)
  enum CallStatus {
    OK,
    NOT_SUPPORTED,
    PENDING,
    ERROR;

    public String getStatus() {
      return this.name();
    }
  }

  @JsonFormat(shape = JsonFormat.Shape.OBJECT)
  enum SmsStatus {
    OK,
    NOT_SUPPORTED,
    INVALID_PHONE_NUMBER,
    ERROR;

    SmsStatus() {
    }

    public String getStatus() {
      return this.name();
    }
  }

  default SmsStatus sendSms(AbstractTemplateSMS abstractTemplateSMS) throws ServerError {
    String message;
    try {
      message = abstractTemplateSMS.processTemplate(getConfiguration());
      for (String phoneNumber : abstractTemplateSMS.getRecipients()) {
        SmsStatus status = sendSms(phoneNumber, message);
        if (status != SmsStatus.OK) {
          return status;
        }
      }
    } catch (SMSException e) {
      throw new ServerError(e);
    }

    return SmsStatus.OK;
  }

  SmsStatus sendSms(@Nonnull String phoneNumber, @Nonnull String message) throws ServerError;

  Configuration getConfiguration();

  CallStatus callParticipant(Ride ride, AvatarType callerType, String callerNumber) throws ServerError;

  CallStatus callParticipant(Ride ride, AvatarType callerType) throws ServerError;

  SmsStatus smsParticipant(Ride ride, AvatarType sender, String message) throws ServerError;
}
