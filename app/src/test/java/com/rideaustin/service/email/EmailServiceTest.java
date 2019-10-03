package com.rideaustin.service.email;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.StringWriter;

import javax.mail.internet.InternetAddress;

import org.apache.commons.mail.EmailException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import com.rideaustin.repo.dsl.UserDslRepository;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class EmailServiceTest {

  private static final String DEFAULT_SENDER_EMAIL = "default@sender.com";
  private static final String DEFAULT_SENDER_NAME = "Default Sender";
  public static final String DEFAULT_BCC_ADDRESS = "bcc@sender.com";

  private EmailService testedInstance;
  @Mock
  private Environment env;
  @Mock
  private Configuration configuration;
  @Mock
  private UserDslRepository userDslRepository;
  @Mock
  private Template template;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    when(env.getProperty(eq("smtp.host"))).thenReturn("smtp.mailtrap.com");
    when(env.getProperty(eq("smtp.port"), eq(Integer.class))).thenReturn(2525);
    when(env.getProperty(eq("smtp.debug"), eq(Boolean.class))).thenReturn(true);
    when(env.getProperty(eq("email.sender.address"))).thenReturn(DEFAULT_SENDER_EMAIL);
    when(env.getProperty(eq("email.sender.name"))).thenReturn(DEFAULT_SENDER_NAME);
    when(env.getProperty(eq("email.bcc"))).thenReturn(DEFAULT_BCC_ADDRESS);
    when(configuration.getTemplate(anyString())).thenReturn(template);
    Mockito.doAnswer(x -> {
      ((StringWriter)x.getArguments()[1]).write("AZAZA");
      return null;
    }).when(template).process(any(), any());
    testedInstance = new EmailService(env, configuration, userDslRepository);
  }

  @Test
  public void testSendEmailSetsDefaultFromAddressAndNameIfNoSpecified() throws Exception {
    EmailStub email = new EmailStub();
    InternetAddress expectedFrom = new InternetAddress(DEFAULT_SENDER_EMAIL, DEFAULT_SENDER_NAME);
    testedInstance.sendEmail(email);

    assertEquals(expectedFrom, email.getFromAddress());
  }

  @Test
  public void testSendEmailSkipsDefaultFromAddressAndNameIfSpecified() throws Exception {
    EmailStub email = new EmailStub();
    String mail = "test@sender.com";
    String name = "Test sender";
    email.setFrom(mail, name);
    InternetAddress expectedFrom = new InternetAddress(mail, name);
    testedInstance.sendEmail(email);

    assertEquals(expectedFrom, email.getFromAddress());
  }

  @Test
  public void testSendEmailSetsBccIfSpecified() throws EmailException {
    EmailStub email = new EmailStub();

    testedInstance.sendEmail(email);

    assertEquals(1, email.getBccAddresses().size());
  }

  private class EmailStub extends AbstractTemplateEmail {

    public EmailStub() {
      this("Test", "template.ftl");
    }

    EmailStub(String subject, String template) {
      super(subject, template);
      addRecipient("test@ra.com");
    }

    @Override
    public String send() throws EmailException {
      return "";
    }
  }

}