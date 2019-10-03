package com.rideaustin.service.thirdparty;

import com.rideaustin.model.enums.PayoneerStatus;
import com.rideaustin.rest.exception.BadRequestException;
import com.rideaustin.rest.exception.ServerError;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.env.Environment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class PayoneerServiceTest {

  private static final String RESPONSE_TEMPLATE = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"yes\"?>" +
    "<GetPayeeDetails><Payee><FirstName>Boris</FirstName><LastName>Tester</LastName><Email>boris.tester@crossover.com</Email>" +
    "<Address1>Red Square, 1</Address1><Address2></Address2><City>Moscow</City><State></State><Zip>127000</Zip><Country>RU</Country>" +
    "<Phone></Phone><Mobile>79205555555</Mobile>" +
    "<PayeeStatus>%s</PayeeStatus>" +
    "<PayOutMethod>iACH</PayOutMethod><RegDate>10/6/2016 5:24:59 AM</RegDate></Payee>" +
    "<CompanyDetails><CompanyName></CompanyName></CompanyDetails></GetPayeeDetails>";

  private static final String ERROR_RESPONSE = "<?xml version='1.0' encoding='ISO-8859-1' ?><GetPayeeDetails>" +
    "<Code>002</Code><Error>Payee does not exist</Error></GetPayeeDetails>";

  private static final String UNKNOWN_ERROR_RESPONSE = "<?xml version='1.0' encoding='ISO-8859-1' ?><GetPayeeDetails>" +
    "<Code>000FFF0</Code><Error>Unknown Error</Error></GetPayeeDetails>";

  private static final String ACTIVE = "Active";
  private static final String INACTIVE = "InActive";

  private PayoneerService testedInstance;

  @Mock
  private Environment env;

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    testedInstance = Mockito.spy(new PayoneerService(env));
    when(env.getProperty("payoneer.baseUrl")).thenReturn("https://api.sandbox.payoneer.com/Payouts/HttpApi/API.aspx");
    when(env.getProperty("payoneer.partnerId")).thenReturn("100053820");
    when(env.getProperty("payoneer.password")).thenReturn("12R9QjL0e4");
    when(env.getProperty("payoneer.username")).thenReturn("RideAustin3820");
    when(env.getProperty("webapp.url")).thenReturn("");
  }

  @Test(expected = BadRequestException.class)
  public void testGetPayoneerStatusThrowsBadRequestExceptionOnEmptyPayoneerId() throws Exception {
    testedInstance.getPayoneerStatus(null);
  }


  @Test
  public void testGetPayoneerStatusRetrievesActiveStatusOnSuccessfulRequest() throws Exception {
    doReturn(String.format(RESPONSE_TEMPLATE, ACTIVE)).when(testedInstance).retrievePayoneerData(anyString());
    PayoneerStatus actual = testedInstance.getPayoneerStatus("56161651");

    assertEquals(PayoneerStatus.ACTIVE, actual);
  }

  @Test
  public void testGetPayoneerStatusRetrievesInActiveStatusOnSuccessfulRequest() throws Exception {
    doReturn(String.format(RESPONSE_TEMPLATE, INACTIVE)).when(testedInstance).retrievePayoneerData(anyString());
    PayoneerStatus actual = testedInstance.getPayoneerStatus("56161651");

    assertEquals(PayoneerStatus.PENDING, actual);
  }

  @Test
  public void testGetPayoneerStatusRetrievesNotRegisteredStatusOnNonExistentPayee() throws Exception {
    doReturn(ERROR_RESPONSE).when(testedInstance).retrievePayoneerData(anyString());
    PayoneerStatus actual = testedInstance.getPayoneerStatus("56161651");

    assertEquals(PayoneerStatus.INITIAL, actual);
  }

  @Test
  public void testGetPayoneerStatusThrowsServerErrorOnUnknownErrors() throws Exception {
    doReturn(UNKNOWN_ERROR_RESPONSE).when(testedInstance).retrievePayoneerData(anyString());
    expectedException.expect(ServerError.class);
    expectedException.expectMessage("Unknown Payoneer error: Unknown Error");

    testedInstance.getPayoneerStatus("565616");
  }
}