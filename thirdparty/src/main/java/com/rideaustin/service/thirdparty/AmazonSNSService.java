package com.rideaustin.service.thirdparty;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.google.common.collect.ImmutableMap;
import com.rideaustin.model.enums.AvatarType;
import com.rideaustin.model.ride.Ride;

import freemarker.template.Configuration;

@Service
public class AmazonSNSService extends BaseCommunicationService {

  public static final String SENDER_ID_KEY = "AWS.SNS.SMS.SenderID";
  public static final String SMS_TYPE_KEY = "AWS.SNS.SMS.SMSType";
  private static final String STRING = "String";
  private static final String TRANSACTIONAL = "Transactional";

  private final AmazonSNSClient amazonSNSClient;
  private final String senderID;

  @Inject
  public AmazonSNSService(AmazonSNSClient amazonSNSClient, Environment environment, Configuration configuration) {
    super(configuration);
    this.amazonSNSClient = amazonSNSClient;
    this.senderID = environment.getProperty("sms.sender.id");
  }

  @Override
  public SmsStatus sendSms(@Nonnull String phoneNumber, @Nonnull String message) {
    amazonSNSClient.publish(createPublishRequest(phoneNumber, message));
    return SmsStatus.OK;
  }

  @Override
  public CallStatus callParticipant(Ride ride, AvatarType callerType, String callerNumber) {
    return CallStatus.NOT_SUPPORTED;
  }

  @Override
  public CallStatus callParticipant(Ride ride, AvatarType callee) {
    return CallStatus.NOT_SUPPORTED;
  }

  @Override
  public SmsStatus smsParticipant(Ride ride, AvatarType sender, String message) {
    return SmsStatus.NOT_SUPPORTED;
  }

  private PublishRequest createPublishRequest(String phoneNumber, String message) {
    return new PublishRequest()
      .withMessage(message)
      .withPhoneNumber(phoneNumber)
      .withMessageAttributes(createMessageAttributes());
  }

  private Map<String, MessageAttributeValue> createMessageAttributes() {
    return ImmutableMap.of(SENDER_ID_KEY, new MessageAttributeValue().withStringValue(senderID).withDataType(STRING),
      SMS_TYPE_KEY, new MessageAttributeValue().withStringValue(TRANSACTIONAL).withDataType(STRING));
  }

}
