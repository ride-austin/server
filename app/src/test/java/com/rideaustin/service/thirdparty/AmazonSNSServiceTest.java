package com.rideaustin.service.thirdparty;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;

import freemarker.template.Configuration;

public class AmazonSNSServiceTest {

  private static final String PHONE_NUMBER = "+12345678901";
  private static final String MESSAGE = "Hello";
  private static final String SENDER = "RideAustin";
  private static final String TRANSACTIONAL = "Transactional";

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  private AmazonSNSService testedInstance;
  @Mock
  private Environment environment;
  @Mock
  private AmazonSNSClient amazonSNSClient;
  @Mock
  private Configuration configuration;

  @Captor
  private ArgumentCaptor<PublishRequest> requestArgumentCaptor;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(environment.getProperty("sms.sender.id")).thenReturn(SENDER);
    testedInstance = new AmazonSNSService(amazonSNSClient, environment, configuration);
  }

  @Test
  public void sendSms() throws Exception {
    testedInstance.sendSms(PHONE_NUMBER, MESSAGE);

    verify(amazonSNSClient).publish(requestArgumentCaptor.capture());
    PublishRequest request = requestArgumentCaptor.getValue();
    assertPublishRequest(request);
  }

  @Test
  public void sendSmsAsTemplate() throws Exception {
    SMSStub sms = new SMSStub();
    sms.addRecipient(PHONE_NUMBER);
    testedInstance.sendSms(sms);

    verify(amazonSNSClient).publish(requestArgumentCaptor.capture());
    PublishRequest request = requestArgumentCaptor.getValue();
    assertPublishRequest(request);
  }

  private void assertPublishRequest(PublishRequest request) {
    assertEquals(MESSAGE, request.getMessage());
    assertEquals(PHONE_NUMBER, request.getPhoneNumber());
    assertTrue(request.getMessageAttributes().containsKey(AmazonSNSService.SENDER_ID_KEY));
    assertTrue(request.getMessageAttributes().containsKey(AmazonSNSService.SMS_TYPE_KEY));
    assertEquals(SENDER, request.getMessageAttributes().get(AmazonSNSService.SENDER_ID_KEY).getStringValue());
    assertEquals(TRANSACTIONAL, request.getMessageAttributes().get(AmazonSNSService.SMS_TYPE_KEY).getStringValue());
  }

  private class SMSStub extends AbstractTemplateSMS {

    public static final String TEMPLATE_FTL = "template.ftl";

    public SMSStub() {
      super(TEMPLATE_FTL);
    }

    @Override
    public String processTemplate(Configuration configuration) throws SMSException {
      return MESSAGE;
    }
  }

}